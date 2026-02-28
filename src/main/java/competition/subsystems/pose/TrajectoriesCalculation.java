package competition.subsystems.pose;

import java.io.File;
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

    public TrajectoriesCalculation(AprilTagFieldLayout aprilTagFieldLayout, PropertyFactory propManager) {
        this.aprilTagFieldLayout = aprilTagFieldLayout;
        this.log = LogManager.getLogger(getClass().getName());

        this.trajectoriesShooterRPMFixed = propManager.createPersistentProperty("trajectoriesShooterRPMFixed", 4800);
    }

    // fieldOrientatedRotation Should tell us where the drivesystem should head.
    // shooterRPM Tell us what the shooting wheel should spin at.
    // ballAngle The angle of the hood based on the ball exit angle.
    public record ShootingData(Rotation2d fieldOrientatedRotation, AngularVelocity shooterRPM, double ballAngle) {
    }

    private record TrajectoryKey(double distance, double shootingSpeed) {
    }

    // hashmap holding all the values
    private static HashMap<TrajectoryKey, HoodTrajectory> trajectoryMap = null;

    // essentially holds all the values for the json to fill hashmap
    public static class HoodTrajectory {
        public double distance;
        public double theta;
        public double velocity;
    }

    public ShootingData calculateAllianceHubShootingData(Pose2d robotPose) {
        Pose2d hubPose = Landmarks.getAllianceHubPose(this.aprilTagFieldLayout,
                DriverStation.getAlliance().orElse(Alliance.Blue));
        return calculateTrajectory(robotPose, hubPose);
    }

    private ShootingData calculateTrajectory(Pose2d robotPose, Pose2d targetPose) {
        if (trajectoryMap == null) {
            loadTrajectories();
        }
        Translation2d vectorToTarget = targetPose.minus(robotPose).getTranslation();
        var finalRotation = vectorToTarget.getNorm() < 0.01 ? robotPose.getRotation() : vectorToTarget.getAngle();
        var finalPose = new Pose2d(robotPose.getX(), robotPose.getY(), finalRotation);

        var shooterPose = finalPose.plus(HOOD_OFFSET_FROM_CENTER_ROBOT);
        double distance = shooterPose.getTranslation().getDistance(targetPose.getTranslation());
        var hoodTrajectory = trajectoryMap.get(new TrajectoryKey(distance, 10.5));

        return new ShootingData(finalRotation, Units.RPM.of(trajectoriesShooterRPMFixed.get()), hoodTrajectory.theta);
    }

    // This method loads the trajectories from the JSON file and populates the
    // HashMap.
    private void loadTrajectories() {
        try {
            File configFile = new File(Filesystem.getDeployDirectory(), "Trajectories.json");

            if (configFile.exists()) {
                ObjectMapper mapper = new ObjectMapper();

                HoodTrajectory[] rawArray = mapper.readValue(configFile, HoodTrajectory[].class);

                trajectoryMap = new HashMap<>();
                for (HoodTrajectory point : rawArray) {
                    trajectoryMap.put(new TrajectoryKey(point.distance, point.velocity), point);
                }

                log.info("Loaded " + trajectoryMap.size() + " trajectories into HashMap.");
            } else {
                log.warn("Trajectories.json not found in the deploy directory!");
            }
        } catch (Exception e) {
            log.error("Failed to load JSON: " + e.getMessage());
        }
    }
}
