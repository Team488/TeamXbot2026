package competition.subsystems.pose;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

//library used for JSON
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.Filesystem;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;
import xbot.common.properties.StringProperty;

@Singleton()
public class TrajectoriesCalculation {
    private static final Transform2d HOOD_OFFSET_FROM_CENTER_ROBOT = new Transform2d(Units.Inches.of(-23.5),
            Units.Inches.of(0), Rotation2d.kZero);

    private final Logger log;
    private final AprilTagFieldLayout aprilTagFieldLayout;
    private final DoubleProperty trajectoriesShooterRPMFixed;
    private final DoubleProperty interpolationFactor;
    private final StringProperty trajectoryCalcVersion;

    @Inject
    public TrajectoriesCalculation(AprilTagFieldLayout aprilTagFieldLayout, PropertyFactory propManager) {
        this.aprilTagFieldLayout = aprilTagFieldLayout;
        this.log = LogManager.getLogger(getClass().getName());
        propManager.setPrefix("TrajectoriesCalculation");
        this.trajectoriesShooterRPMFixed = propManager.createPersistentProperty("trajectoriesShooterRPMFixed", 4800);
        this.interpolationFactor = propManager.createPersistentProperty("AllianceZoneAimMidpointInterpolationFactor",
                0.5);
        this.trajectoryCalcVersion = propManager.createPersistentProperty("TrajectoryCalcVersion", "2");
    }

    // fieldOrientatedRotation Should tell us where the drive system should head.
    // shooterRPM Tell us what the shooting wheel should spin at.
    // servoRatio The desired servo ratio of the hood for the correct exit angle
    public record ShootingData(Rotation2d fieldOrientatedRotation, AngularVelocity shooterRPM, double servoRatio) {
    }

    private static final ShootingData emptyShootingData = new ShootingData(Rotation2d.kZero, Units.RPM.of(0), 0.0);

    private record TrajectoryKey(double distance, double shootingSpeed) {
    }

    // hashmap holding all the values
    private static HashMap<TrajectoryKey, HoodTrajectory> trajectoryMap = null;

    // essentially holds all the values for the JSON to fill hashmap
    public static class HoodTrajectory {
        public double distance;
        public double servoRatio;
        public double velocity;
    }

    public enum PresetShootingDistance {
        NEAR,
        TOWER_CLOSE,
        TOWER_FAR,
        TRENCH
    }

    public ShootingData calculateAllianceHubShootingData(Pose2d robotPose) {
        Pose2d hubPose = Landmarks.getAllianceHubPose(this.aprilTagFieldLayout,
                DriverStation.getAlliance().orElse(Alliance.Blue));
        return calculateTrajectory(robotPose, hubPose);
    }

    public ShootingData calculateAllianceZoneShootingData(Pose2d robotPose) {
        Pose2d closestTrenchNeutralSideIdPose = Landmarks.getClosestTrenchNeutralSideIdPose(
                aprilTagFieldLayout,
                DriverStation.getAlliance().orElse(Alliance.Blue),
                robotPose);

        Translation2d target = Landmarks
                .getAllianceHubPose(aprilTagFieldLayout, DriverStation.getAlliance().orElse(Alliance.Blue))
                .getTranslation()
                .interpolate(closestTrenchNeutralSideIdPose.getTranslation(), interpolationFactor.get());

        Pose2d targetPose = new Pose2d(target, Rotation2d.kZero);

        return calculateTrajectory(robotPose, targetPose);
    }

    private ShootingData calculateTrajectory(Pose2d robotPose, Pose2d targetPose) {
        switch (trajectoryCalcVersion.get()) {
            case "1":
                return this.calculateTrajectoryV1DumbFixedArcToHub(robotPose, targetPose);
            case "2":
                return this.calculateTrajectoryV2KnownDistance(robotPose, targetPose);
            case "3":
            default:
                return this.calculateTrajectoryV3Dynamic(robotPose, targetPose);
        }
    }

    private Rotation2d rotationToShootFrom(Pose2d robotPose, Pose2d targetPose) {
        Translation2d vectorToTarget = targetPose.minus(robotPose).getTranslation();

        return vectorToTarget.getNorm() < 0.01 ? robotPose.getRotation() : vectorToTarget.getAngle();
    }

    // Fixed shooter parameters. Should only be used when things to very wrong.
    private ShootingData calculateTrajectoryV1DumbFixedArcToHub(Pose2d robotPose, Pose2d targetPose) {
        return new ShootingData(this.rotationToShootFrom(robotPose, targetPose), Units.RPM.of(3800), 0.2);
    }

    // Look up known distances based on a preset distances rather than continuous
    // adjustment.
    private ShootingData calculateTrajectoryV2KnownDistance(Pose2d robotPose, Pose2d targetPose) {
        Translation2d vectorToTarget = targetPose.minus(robotPose).getTranslation();
        var preset = this.getPresetShootingDistance(Units.Meters.of(vectorToTarget.getNorm()));
        var shootingData = this.getKnownShootingParameters(preset);

        // Target rotation will probably be ignored or corrected if we're using manual
        // distance, but point towards where we think the hub
        return new ShootingData(this.rotationToShootFrom(robotPose, targetPose), shootingData.shooterRPM,
                shootingData.servoRatio);
    }

    // Look up optimal shooting parameters based on current pose and shooting
    // target's pose.
    private ShootingData calculateTrajectoryV3Dynamic(Pose2d robotPose, Pose2d targetPose) {
        if (trajectoryMap == null) {
            loadTrajectories();
        }
        Rotation2d finalRotation = this.rotationToShootFrom(robotPose, targetPose);
        Pose2d finalPose = new Pose2d(robotPose.getX(), robotPose.getY(), finalRotation);

        Pose2d shooterPose = finalPose.plus(HOOD_OFFSET_FROM_CENTER_ROBOT);
        double distance = shooterPose.getTranslation().getDistance(targetPose.getTranslation());
        var key = new TrajectoryKey(distance, 10.5);
        if (!trajectoryMap.containsKey(key)) {
            log.warn(
                    "Trajectory not found, potentially trajectories.json not found or the value doesn't exist in trajectories!");
            return TrajectoriesCalculation.emptyShootingData;
        }
        HoodTrajectory hoodTrajectory = trajectoryMap.get(key);

        return new ShootingData(finalRotation, Units.RPM.of(trajectoriesShooterRPMFixed.get()),
                hoodTrajectory.servoRatio);
    }

    // Known poses on the field that are good to shoot from.
    private ShootingData getKnownShootingParameters(PresetShootingDistance presetDistance) {
        switch (presetDistance) {
            case TOWER_FAR:
                return new ShootingData(
                        new Rotation2d(0),
                        Units.RPM.of(3800),
                        0.25);
            case TOWER_CLOSE:
                return new ShootingData(
                        new Rotation2d(0),
                        Units.RPM.of(3600),
                        0.0);
            case TRENCH:
                return new ShootingData(
                        new Rotation2d(0),
                        Units.RPM.of(3800),
                        0.2);
            case NEAR:
            default:
                return new ShootingData(
                        new Rotation2d(0),
                        Units.RPM.of(2600),
                        0.0);
        }
    }

    private record PresetShootingDistanceLookup(Distance distance, PresetShootingDistance presetShootingDistance) {
    }

    // Convert numeric distance to known distance presets.
    private PresetShootingDistance getPresetShootingDistance(Distance distance) {
        var presets = List.of(
                // Note: robot radius corrections below are educated guesses but don't matter
                // precisely here.
                new PresetShootingDistanceLookup(Units.Meters.of(0.94 + 0.34), PresetShootingDistance.NEAR),
                new PresetShootingDistanceLookup(Units.Meters.of(3.34 + 0.34), PresetShootingDistance.TRENCH),
                new PresetShootingDistanceLookup(Units.Meters.of(3.52 - 0.60), PresetShootingDistance.TOWER_CLOSE),
                new PresetShootingDistanceLookup(Units.Meters.of(3.52 + 0.34), PresetShootingDistance.TOWER_FAR));
        return presets.stream()
                .min(Comparator.comparingDouble(
                        preset -> Math.abs(distance.abs(Units.Meter) - preset.distance.abs(Units.Meter))))
                .map(PresetShootingDistanceLookup::presetShootingDistance)
                .orElse(PresetShootingDistance.NEAR);
    }
    // This method loads the trajectories from the JSON file and populates the
    // HashMap.
    private void loadTrajectories() {
        trajectoryMap = new HashMap<>();

        try {
            File configFile = new File(Filesystem.getDeployDirectory(), "Trajectories.json");

            if (configFile.exists()) {
                ObjectMapper mapper = new ObjectMapper();

                HoodTrajectory[] rawArray = mapper.readValue(configFile, HoodTrajectory[].class);

                for (HoodTrajectory point : rawArray) {
                    trajectoryMap.put(new TrajectoryKey(point.distance, point.velocity), point);
                }

                log.info("Loaded {} trajectories into HashMap.", trajectoryMap.size());
            } else {
                log.warn("Trajectories.json not found in the deploy directory!");
            }
        } catch (Exception e) {
            log.error("Failed to load JSON: {}", e.getMessage());
        }
    }
}
