package competition.subsystems.hood.commands;

import java.io.File;
import java.util.HashMap;

import javax.inject.Inject;

//library used for JSON 
import com.fasterxml.jackson.databind.ObjectMapper;

import competition.subsystems.hood.HoodSubsystem;
import edu.wpi.first.wpilibj.Filesystem;
import xbot.common.command.BaseCommand;

public class CorrectHoodAngleCommand extends BaseCommand {
    
    final HoodSubsystem hood;
    //essentially holds all the values for the json to fill hashmap
    public static class HoodTrajectory {
        public double distance;
        public double theta;
        public double velocity;
    }

    //hashmap holding all the values
    private HashMap<Double, HoodTrajectory> trajectoryMap = null;
    
    @Inject
    public CorrectHoodAngleCommand(HoodSubsystem hoodSubsystem) {
        this.hood = hoodSubsystem;
        this.addRequirements(hoodSubsystem); 
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

        double currentDistance = 0; // placeholder value
        log.info("Current distance: " + currentDistance);

        HoodTrajectory bestShot = null;
        // variable to keep track of the smallest difference found so far
        double minDiff = Double.MAX_VALUE;

        if (trajectoryMap != null) {
            for (Double keyDistance : trajectoryMap.keySet()) {

                double diff = Math.abs(currentDistance - keyDistance);
                
                if (diff < minDiff) {
                    minDiff = diff;
                    //get the value for whatever it is closest to the current distance 
                    bestShot = trajectoryMap.get(keyDistance);
                }
            }
        } else {
            log.warn("Trajectory map is null!");
        }

        if (bestShot != null) {
            log.info("Best shot - Distance: " + bestShot.distance + 
                     ", Theta: " + bestShot.theta + 
                     ", Velocity: " + bestShot.velocity +
                     " (diff: " + minDiff + ")");
            hood.runServo();
        } else {
            log.warn("No matching trajectory found!");
        }
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
                    trajectoryMap.put(point.distance, point);
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
        return true;
    }
}