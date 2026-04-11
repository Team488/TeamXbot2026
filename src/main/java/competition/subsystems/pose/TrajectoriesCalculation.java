package competition.subsystems.pose;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

//library used for JSON
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.wpi.first.math.interpolation.Interpolatable;
import edu.wpi.first.math.interpolation.InterpolatingTreeMap;
import edu.wpi.first.math.interpolation.InverseInterpolator;
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

import static edu.wpi.first.units.Units.RPM;

@Singleton()
public class TrajectoriesCalculation {
    private static final Transform2d HOOD_OFFSET_FROM_CENTER_ROBOT = new Transform2d(Units.Inches.of(-23.5),
            Units.Inches.of(0), Rotation2d.kZero);
    private static Map<PresetShootingDistance, PresetShootingProperties> presetShootingLookup;

    private final Logger log;
    private final AprilTagFieldLayout aprilTagFieldLayout;
    private final DoubleProperty trajectoriesShooterRPMFixed;
    private final DoubleProperty interpolationFactor;
    private final DoubleProperty v3DistanceOffsetMeters;
    private final StringProperty trajectoryCalcVersion;

    private static void getOrCreatePresetLookup(PropertyFactory propManager) {
        if (presetShootingLookup == null) {
            presetShootingLookup = Map.of(PresetShootingDistance.NEAR,
                    new PresetShootingProperties(
                            propManager.createPersistentProperty("PresetShooting.NEAR RPM", 2725),
                            propManager.createPersistentProperty("PresetShooting.NEAR Hood ServoRatio", 0.0)),
                    PresetShootingDistance.TOWER_CLOSE,
                    new PresetShootingProperties(
                            propManager.createPersistentProperty("PresetShooting.TOWER_CLOSE RPM", 3400),
                            propManager.createPersistentProperty("PresetShooting.TOWER_CLOSE Hood ServoRatio", 0.2)),
                    PresetShootingDistance.TOWER_FAR,
                    new PresetShootingProperties(
                            propManager.createPersistentProperty("PresetShooting.TOWER_FAR RPM", 3900),
                            propManager.createPersistentProperty("PresetShooting.TOWER_FAR Hood ServoRatio", 0.0)),
                    PresetShootingDistance.TRENCH,
                    new PresetShootingProperties(
                            propManager.createPersistentProperty("PresetShooting.TRENCH RPM", 4100),
                            propManager.createPersistentProperty("PresetShooting.TRENCH Hood ServoRatio", 0.0)),
                    PresetShootingDistance.CORNER,
                    new PresetShootingProperties(
                            propManager.createPersistentProperty("PresetShooting.CORNER RPM", 3750),
                            propManager.createPersistentProperty("PresetShooting.CORNER Hood ServoRatio", 1)));

        }
    }

    @Inject
    public TrajectoriesCalculation(AprilTagFieldLayout aprilTagFieldLayout, PropertyFactory propManager) {
        this.aprilTagFieldLayout = aprilTagFieldLayout;
        this.log = LogManager.getLogger(getClass().getName());
        propManager.setPrefix("TrajectoriesCalculation");
        this.trajectoriesShooterRPMFixed = propManager.createPersistentProperty("trajectoriesShooterRPMFixed", 4800);
        this.interpolationFactor = propManager.createPersistentProperty("AllianceZoneAimMidpointInterpolationFactor",
                0.5);
        this.trajectoryCalcVersion = propManager.createPersistentProperty("TrajectoryCalcVersion", "3");
        this.v3DistanceOffsetMeters = propManager.createPersistentProperty("v3DistanceOffsetMeters", 0.45);
        getOrCreatePresetLookup(propManager);
    }

    // fieldOrientatedRotation Should tell us where the drive system should head.
    // shooterRPM Tell us what the shooting wheel should spin at.
    // servoRatio The desired servo ratio of the hood for the correct exit angle
    public record ShootingData(Rotation2d fieldOrientatedRotation, AngularVelocity shooterRPM, double servoRatio) {
    }

    public record PresetShootingData(AngularVelocity shooterRPM, double hoodServoRatio) { }

    private record PresetShootingProperties(DoubleProperty shooterRpmProperty, DoubleProperty hoodServoRatio) {
    }

    private static final ShootingData emptyShootingData = new ShootingData(Rotation2d.kZero, RPM.of(0), 0.0);

    private record TrajectoryKey(double distance) {
    }

    // hashmaps holding all the values
    private static InterpolatingTreeMap<Double, HoodTrajectory> trajectoryMap;
    private static InterpolatingTreeMap<Double, HoodTrajectory> trajectoryZeroHoodMap;

    // CHECKSTYLE:OFF
    // essentially holds all the values for the JSON to fill hashmap
    public static class HoodTrajectory implements Interpolatable<HoodTrajectory> {
        public double distance;
        public double theta;
        public double servo;
        public double velocity;
        public double RPM;

        @Override
        public HoodTrajectory interpolate(HoodTrajectory endValue, double t) {
            HoodTrajectory result = new HoodTrajectory();
            result.RPM = this.RPM + (endValue.RPM - this.RPM) * t; // Point slope equation
            result.servo = this.servo + (endValue.servo - this.servo) * t; // Point slope equation

            return result;
        }
    }

    // CHECKSTYLE:ON

    public enum PresetShootingDistance {
        NEAR,
        TOWER_CLOSE,
        TOWER_FAR,
        TRENCH,
        CORNER
    }

    public ShootingData calculateAllianceHubShootingData(Pose2d robotPose, boolean zeroHood) {
        Pose2d hubPose = Landmarks.getAllianceHubPose(this.aprilTagFieldLayout,
                DriverStation.getAlliance().orElse(Alliance.Blue));
        return calculateTrajectory(robotPose, hubPose, zeroHood);
    }

    public ShootingData calculateAllianceZoneShootingData(Pose2d robotPose, boolean zeroHood) {
        Pose2d closestTrenchNeutralSideIdPose = Landmarks.getClosestTrenchNeutralSideIdPose(
                aprilTagFieldLayout,
                DriverStation.getAlliance().orElse(Alliance.Blue),
                robotPose);

        Translation2d target = Landmarks
                .getAllianceHubPose(aprilTagFieldLayout, DriverStation.getAlliance().orElse(Alliance.Blue))
                .getTranslation()
                .interpolate(closestTrenchNeutralSideIdPose.getTranslation(), interpolationFactor.get());

        Pose2d targetPose = new Pose2d(target, Rotation2d.kZero);

        return calculateTrajectory(robotPose, targetPose, zeroHood);
    }

    public ShootingData calculateAllianceZoneShootingDataV4(Pose2d robotPose, boolean zeroHood) {
        Pose2d closestTrenchNeutralSideIdPose = Landmarks.getClosestTrenchNeutralSideIdPose(
                aprilTagFieldLayout,
                DriverStation.getAlliance().orElse(Alliance.Blue),
                robotPose);

        Translation2d target = Landmarks
                .getAllianceHubPose(aprilTagFieldLayout, DriverStation.getAlliance().orElse(Alliance.Blue))
                .getTranslation()
                .interpolate(closestTrenchNeutralSideIdPose.getTranslation(), interpolationFactor.get());

        Pose2d targetPose = new Pose2d(target, Rotation2d.kZero);

        return calculateTrajectory(robotPose, targetPose, zeroHood);
    }

    public PresetShootingData getPresetShootingSettings(PresetShootingDistance shootingDistance) {
        var mapLookup = presetShootingLookup.get(shootingDistance);

        return new PresetShootingData(
                RPM.of(mapLookup.shooterRpmProperty.get()),
                mapLookup.hoodServoRatio.get());
    }

    private ShootingData calculateTrajectory(Pose2d robotPose, Pose2d targetPose, boolean zeroHood) {
        return switch (trajectoryCalcVersion.get()) {
            case "1" -> this.calculateTrajectoryV1DumbFixedArcToHub(robotPose, targetPose);
            case "2" -> this.calculateTrajectoryV2KnownDistance(robotPose, targetPose);
            default -> this.calculateTrajectoryV4Dynamic(robotPose, targetPose, zeroHood);
        };
    }

    private Rotation2d rotationToShootFrom(Pose2d robotPose, Pose2d targetPose) {
        Translation2d vectorToTarget = targetPose.minus(robotPose).getTranslation();

        return vectorToTarget.getNorm() < 0.01 ? robotPose.getRotation() : vectorToTarget.getAngle();
    }

    // Fixed shooter parameters. Should only be used when things to very wrong.
    private ShootingData calculateTrajectoryV1DumbFixedArcToHub(Pose2d robotPose, Pose2d targetPose) {
        return new ShootingData(this.rotationToShootFrom(robotPose, targetPose), RPM.of(3800), 0.2);
    }

    // Look up known distances based on a preset distances rather than continuous
    // adjustment.
    private ShootingData calculateTrajectoryV2KnownDistance(Pose2d robotPose, Pose2d targetPose) {
        Translation2d vectorToTarget = targetPose.minus(robotPose).getTranslation();
        var preset = this.getPresetShootingDistance(Units.Meters.of(vectorToTarget.getNorm()));
        var shootingData = this.getPresetShootingSettings(preset);

        // Target rotation will probably be ignored or corrected if we're using manual
        // distance, but point towards where we think the hub
        return new ShootingData(this.rotationToShootFrom(robotPose, targetPose), shootingData.shooterRPM,
                shootingData.hoodServoRatio);
    }

    private double roundingDistance(double distance) {
        return Math.round(distance * 100.0) / 100.0;
    }

    private ShootingData calculateTrajectoryV3Dynamic(Pose2d robotPose, Pose2d targetPose, boolean zeroHood) {
        // This does take a performance hit, so we should consider loading these in when we aren't doing anything.
        if (zeroHood) {
            if (trajectoryZeroHoodMap == null) {
                trajectoryZeroHoodMap = loadTrajectoriesV4("trajectories_0_hood.json");
            }
        } else {
            if (trajectoryMap == null) {
                trajectoryMap = loadTrajectoriesV4("trajectories.json");
            }
        }
        Rotation2d finalRotation = this.rotationToShootFrom(robotPose, targetPose);
        Pose2d finalPose = new Pose2d(robotPose.getX(), robotPose.getY(), finalRotation);

        Pose2d shooterPose = finalPose.plus(HOOD_OFFSET_FROM_CENTER_ROBOT);
        double distance = shooterPose.getTranslation().getDistance(targetPose.getTranslation());
        var roundedDistance = roundingDistance(distance);
        var offsetDistance = roundedDistance + v3DistanceOffsetMeters.get();
        var key = new TrajectoryKey(roundingDistance(offsetDistance));
        var hoodTrajectory = this.searchForHoodTrajectory(key, zeroHood);
        if (hoodTrajectory.isEmpty()) {
            if (zeroHood) {
                log.warn(
                        "Trajectory not found, potentially trajectories_0_hood.json not found or the value doesn't exist in trajectories for distance.");
            } else {
                log.warn(
                        "Trajectory not found, potentially trajectories.json not found or the value doesn't exist in trajectories for distance.");
            }
            return TrajectoriesCalculation.emptyShootingData;
        }
        var matchedTrajectory = hoodTrajectory.get();

        log.info("Returning shooting speed: {}, servo: {}", matchedTrajectory.RPM, matchedTrajectory.servo);

        return new ShootingData(finalRotation, Units.RPM.of(matchedTrajectory.RPM), matchedTrajectory.servo);
    }

    // Look up optimal shooting parameters based on current pose and shooting
    // target's pose.
    private ShootingData calculateTrajectoryV4Dynamic(Pose2d robotPose, Pose2d targetPose, boolean zeroHood) {
        if (trajectoryMap == null) {
            trajectoryMap = loadTrajectoriesV4("trajectories.json");
        }
        if (trajectoryZeroHoodMap == null) {
            trajectoryZeroHoodMap = loadTrajectoriesV4("trajectories_0_hood.json");
        }

        InterpolatingTreeMap<Double, HoodTrajectory> activeMap = zeroHood ? trajectoryZeroHoodMap : trajectoryMap;

        Rotation2d finalRotation = this.rotationToShootFrom(robotPose, targetPose);
        Pose2d finalPose = new Pose2d(robotPose.getX(), robotPose.getY(), finalRotation);
        Pose2d shooterPose = finalPose.plus(HOOD_OFFSET_FROM_CENTER_ROBOT);
        double distance = shooterPose.getTranslation().getDistance(targetPose.getTranslation());
        var offsetDistance = distance + v3DistanceOffsetMeters.get();

        HoodTrajectory result = activeMap.get(offsetDistance);
        if (result == null) {
            log.warn("Trajectory not found for distance: {}", offsetDistance);
            return TrajectoriesCalculation.emptyShootingData;
        }
        return new ShootingData(finalRotation, Units.RPM.of(result.RPM), result.servo);
    }

    private Optional<HoodTrajectory> searchForHoodTrajectory(TrajectoryKey key, boolean zeroHood) {

        if (zeroHood) {
            log.error(
                    "Trajectory not found, potentially trajectories_0_hood.json not found or the value doesn't exist in trajectories!");
        } else {
            log.error(
                    "Trajectory not found, potentially trajectories.json not found or the value doesn't exist in trajectories!");
        }
        return Optional.empty();
    }

    private record PresetShootingDistanceLookup(Distance distance, PresetShootingDistance presetShootingDistance) {
    }

    // Convert numeric distance to known distance presets.
    private PresetShootingDistance getPresetShootingDistance(Distance distance) {
        var presets = List.of(
                // Note: robot radius corrections below are educated guesses but don't matter
                // precisely here.
                new PresetShootingDistanceLookup(Units.Meters.of(0.94 + 0.34), PresetShootingDistance.NEAR),
                new PresetShootingDistanceLookup(Units.Meters.of(2.74 + 0.34), PresetShootingDistance.TRENCH),
                new PresetShootingDistanceLookup(Units.Meters.of(3.52 - 0.60), PresetShootingDistance.TOWER_CLOSE),
                new PresetShootingDistanceLookup(Units.Meters.of(2.64 + 0.34), PresetShootingDistance.TOWER_FAR),
                new PresetShootingDistanceLookup(Units.Meters.of(6.27 - 0.6), PresetShootingDistance.CORNER));
        return presets.stream()
                .min(Comparator.comparingDouble(
                        preset -> Math.abs(distance.abs(Units.Meter) - preset.distance.abs(Units.Meter))))
                .map(PresetShootingDistanceLookup::presetShootingDistance)
                .orElse(PresetShootingDistance.NEAR);
    }

    // This method loads the trajectories from the JSON file and populates the
    // HashMap.
    private InterpolatingTreeMap<Double, HoodTrajectory> loadTrajectoriesV4(String filename) {
        InterpolatingTreeMap<Double, HoodTrajectory> map =
                new InterpolatingTreeMap<>(InverseInterpolator.forDouble(), Interpolatable::interpolate);
        try {
            File configFile = new File(Filesystem.getDeployDirectory(), filename);
            if (configFile.exists()) {
                ObjectMapper mapper = new ObjectMapper();
                HoodTrajectory[] rawArray = mapper.readValue(configFile, HoodTrajectory[].class);
                for (HoodTrajectory point : rawArray) {
                    map.put(point.distance, point);
                }
                log.info("Loaded trajectories from {}", filename);
            } else {
                log.warn("{} not found in the deploy directory!", filename);
            }
        } catch (Exception e) {
            log.error("Failed to load JSON from {}: {}", filename, e.getMessage());
        }
        return map; // always returns map, even if empty
    }

    private HashMap<TrajectoryKey, HoodTrajectory> loadTrajectories(String filename) {
        var loadedMap = new HashMap<TrajectoryKey, HoodTrajectory>();

        try {
            File configFile = new File(Filesystem.getDeployDirectory(), filename);

            if (configFile.exists()) {
                ObjectMapper mapper = new ObjectMapper();

                HoodTrajectory[] rawArray = mapper.readValue(configFile, HoodTrajectory[].class);

                for (HoodTrajectory point : rawArray) {
                    var roundedDistance = Math.round(point.distance * 100.0) / 100.0;
                    var key = new TrajectoryKey(roundedDistance);
                    if (!loadedMap.containsKey(key)) {
                        loadedMap.put(key, point);
                    }
                }

                log.info("Loaded {} trajectories from {} into HashMap.", loadedMap.size(), filename);
            } else {
                log.warn("{} not found in the deploy directory!", filename);
            }
        } catch (Exception e) {
            log.error("Failed to load JSON from {}: {}", filename, e.getMessage());
        }

        return loadedMap;
    }


}