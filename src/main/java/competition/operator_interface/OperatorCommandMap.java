package competition.operator_interface;

import javax.inject.Inject;
import javax.inject.Singleton;

import competition.simulation.commands.ResetSimulatedPoseCommand;
import xbot.common.controls.sensors.XXboxController;
import xbot.common.subsystems.pose.commands.SetRobotHeadingCommand;

/**
 * Maps operator interface buttons to commands
 */
@Singleton
public class OperatorCommandMap {

    @Inject
    public OperatorCommandMap() {
    }

    @Inject
    public void setupDriveCommands(OperatorInterface operatorInterface,
                                   SetRobotHeadingCommand resetHeading) {
        resetHeading.setHeadingToApply(0);
        operatorInterface.driverGamepad.getifAvailable(XXboxController.XboxButton.Start).onTrue(resetHeading);

        // TODO: map driver buttons to commands here
    }

    @Inject
    public void setupOperatorGamepad(OperatorInterface operatorInterface) {
        // TODO: map operator buttons to commands here
    }

    @Inject
    public void setupAutoCommands() {
        // TODO: create autonomous routines and add them to the dashboard here
    }

    @Inject
    public void setupSimulatorCommands(
            ResetSimulatedPoseCommand resetPose) {
        resetPose.includeOnSmartDashboard();
    }
}
