package competition.subsystems.hood.commands;

import java.io.File;
import java.util.HashMap;

import javax.inject.Inject;

//library used for JSON 
import com.fasterxml.jackson.databind.ObjectMapper;

import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.Filesystem;
import xbot.common.command.BaseCommand;

public class UpdateHoodAngleFromPositionCommand extends BaseCommand {
    private static double MAX_ANGLE_OF_HOOD = 73.0;
    private static double MIN_ANGLE_OF_HOOD = 39.6;
    private static double RANGE_OF_ANGLE_OF_HOOD = MAX_ANGLE_OF_HOOD - MIN_ANGLE_OF_HOOD;

    private record TrajectoryKey(double distance, double shootingSpeed) {}

    private final PoseSubsystem pose;
    private final AprilTagFieldLayout aprilTagFieldLayout;
    private double targetRatio = 0.0;
    
    final HoodSubsystem hood;
    //essentially holds all the values for the json to fill hashmap
    public static class HoodTrajectory {
        public double distance;
        public double theta;
        public double velocity;
    }

    //hashmap holding all the values
    private static HashMap<TrajectoryKey, HoodTrajectory> trajectoryMap = null;
    
    @Inject
    public UpdateHoodAngleFromPositionCommand(PoseSubsystem pose, HoodSubsystem hoodSubsystem, AprilTagFieldLayout aprilTagFieldLayout) {
        this.pose = pose;
        this.aprilTagFieldLayout = aprilTagFieldLayout;
        this.hood = hoodSubsystem;
        this.addRequirements(hoodSubsystem);

        // TODO: If this slows down initial boot time then we should move this into initialize or
        // some other periodic. (Maybe in a disable periodic)
        if (trajectoryMap == null) {
            loadTrajectories();
        }
    }

    @Override
    public void initialize() {
        log.info("Initializing Hood Alignment");

        if (trajectoryMap == null) {
            loadTrajectories();
        }
    }

    @Override
    public void execute() {
        Pose2d hubPose = this.pose.getAllianceHubPose(this.aprilTagFieldLayout, DriverStation.getAlliance().orElse(Alliance.Blue));
        double currentDistance = this.pose.getCurrentPose2d().getTranslation().getDistance(hubPose.getTranslation());
        // TODO: Fill out velocity based on shooting wheel speed and potential trajectory.
        var hoodTrajectory = trajectoryMap.get(new TrajectoryKey(currentDistance, 10.5));
        var theta = hoodTrajectory.theta;
        this.targetRatio = (MAX_ANGLE_OF_HOOD - theta ) / RANGE_OF_ANGLE_OF_HOOD;
        this.hood.setTargetValue(this.targetRatio);
    }

    // This method loads the trajectories from the JSON file and populates the HashMap.
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
    @Override
    public boolean isFinished() {
        return this.targetRatio == this.hood.getCurrentValue();
    }
}
