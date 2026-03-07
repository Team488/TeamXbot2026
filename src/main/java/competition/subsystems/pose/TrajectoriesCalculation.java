package competition.subsystems.pose;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

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
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.Filesystem;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

@Singleton()
public class TrajectoriesCalculation {
    private static final Transform2d HOOD_OFFSET_FROM_CENTER_ROBOT = new Transform2d(Units.Inches.of(-23.5),
            Units.Inches.of(0), Rotation2d.kZero);

    private final Logger log;
    private final AprilTagFieldLayout aprilTagFieldLayout;
    private final DoubleProperty trajectoriesShooterRPMFixed;
    private final DoubleProperty interpolationFactor;

    public TrajectoriesCalculation(AprilTagFieldLayout aprilTagFieldLayout, PropertyFactory propManager) {
        this.aprilTagFieldLayout = aprilTagFieldLayout;
        this.log = LogManager.getLogger(getClass().getName());

        this.trajectoriesShooterRPMFixed = propManager.createPersistentProperty("trajectoriesShooterRPMFixed", 4800);
        this.interpolationFactor = propManager.createPersistentProperty("AllianceZoneAimMidpointInterpolationFactor", 0.5);
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

    public ShootingData calculateAllianceHubShootingData(Pose2d robotPose) {
        Pose2d hubPose = Landmarks.getAllianceHubPose(this.aprilTagFieldLayout,
                DriverStation.getAlliance().orElse(Alliance.Blue));
        return calculateTrajectory(robotPose, hubPose);
    }

    public ShootingData calculateAllianceZoneShootingData(Pose2d robotPose) {
        Pose2d closestTrenchNeutralSideIdPose = Landmarks.getClosestTrenchNeutralSideIdPose(
                aprilTagFieldLayout,
                DriverStation.getAlliance().orElse(Alliance.Blue),
                robotPose
        );

        Translation2d target = Landmarks.getAllianceHubPose(aprilTagFieldLayout, DriverStation.getAlliance().orElse(Alliance.Blue))
                .getTranslation()
                .interpolate(closestTrenchNeutralSideIdPose.getTranslation(), interpolationFactor.get());

        Pose2d targetPose = new Pose2d(target, Rotation2d.kZero);

        return calculateTrajectory(robotPose, targetPose);
    }

    private ShootingData calculateTrajectory(Pose2d robotPose, Pose2d targetPose) {
        return calculateTrajectoryV3Dynamic(robotPose, targetPose);
    }

    // Fixed shooter parameters. Should only be used when things to very wrong.
    private ShootingData calculateTrajectoryV1DumbFixedArcToHub(Pose2d robotPose) {
        Pose2d hubPose = Landmarks.getAllianceHubPose(this.aprilTagFieldLayout,
                DriverStation.getAlliance().orElse(Alliance.Blue));
        Translation2d vectorToTarget = hubPose.minus(robotPose).getTranslation();
        Rotation2d finalRotation = vectorToTarget.getNorm() < 0.01 ? robotPose.getRotation() : vectorToTarget.getAngle();

        return new ShootingData(finalRotation, Units.RPM.of(/* TODO GET FROM JOSH */488), /* TODO GET FROM JOSH */0.0);
    }

    // "Snap" robot pose to known tested points from which to shoot.
    // private ShootingData calculateTrajectoryV2KnownPointsToHub(Pose2d robotPose) {
    //     Pose2d hubPose = Landmarks.getAllianceHubPose(this.aprilTagFieldLayout,
    //             DriverStation.getAlliance().orElse(Alliance.Blue));
    //     Translation2d vectorToTarget = hubPose.minus(robotPose).getTranslation();
    //     Rotation2d finalRotation = vectorToTarget.getNorm() < 0.01 ? robotPose.getRotation() : vectorToTarget.getAngle();
    //     Pose2d finalPose = new Pose2d(robotPose.getX(), robotPose.getY(), finalRotation);
    //     Pose2d shooterPose = finalPose.plus(HOOD_OFFSET_FROM_CENTER_ROBOT);

    //     // shooterPose.nearest(knownShootingPositions());


    //     return new ShootingData(finalRotation, Units.RPM.of(trajectoriesShooterRPMFixed.get()), hoodTrajectory.servoRatio);
    // }

    // Look up optimal shooting parameters based on current pose and shooting target's pose.
    private ShootingData calculateTrajectoryV3Dynamic(Pose2d robotPose, Pose2d targetPose) {
        if (trajectoryMap == null) {
            loadTrajectories();
        }
        Translation2d vectorToTarget = targetPose.minus(robotPose).getTranslation();
        Rotation2d finalRotation = vectorToTarget.getNorm() < 0.01 ? robotPose.getRotation() : vectorToTarget.getAngle();
        Pose2d finalPose = new Pose2d(robotPose.getX(), robotPose.getY(), finalRotation);

        Pose2d shooterPose = finalPose.plus(HOOD_OFFSET_FROM_CENTER_ROBOT);
        double distance = shooterPose.getTranslation().getDistance(targetPose.getTranslation());
        var key = new TrajectoryKey(distance, 10.5);
        if (!trajectoryMap.containsKey(key)) {
            log.warn("Trajectory not found, potentially trajectories.json not found or the value doesn't exist in trajectories!");
            return TrajectoriesCalculation.emptyShootingData;
        }
        HoodTrajectory hoodTrajectory = trajectoryMap.get(key);

        return new ShootingData(finalRotation, Units.RPM.of(trajectoriesShooterRPMFixed.get()), hoodTrajectory.servoRatio);
    }

    // // Known poses on the field that are good to shoot from.
    // private Collection<Pose2d> knownShootingPositions() {

    // }

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
