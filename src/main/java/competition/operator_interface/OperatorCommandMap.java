package competition.operator_interface;

import javax.inject.Inject;
import javax.inject.Singleton;

import competition.auto.DriveToOutpostAuto;
import competition.subsystems.drive.commands.DriveToOutpostCommand;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import xbot.common.simulation.ResetSimulatorPositionCommand;
import xbot.common.subsystems.pose.commands.SetRobotHeadingCommand;

/**
 * Maps operator interface buttons to commands
 */
@Singleton
public class OperatorCommandMap {

    @Inject
    public OperatorCommandMap() {}
    
    // Example for setting up a command to fire when a button is pressed:
    @Inject
    public void setupMyCommands(
            OperatorInterface operatorInterface,
            SetRobotHeadingCommand resetHeading) {
        resetHeading.setHeadingToApply(0);
        operatorInterface.gamepad.getifAvailable(1).onTrue(resetHeading);
    }
    @Inject
    public void setupSimulatorCommands(
            ResetSimulatorPositionCommand resetSimulatorPositionCommand,
            DriveToOutpostCommand driveToOutpostCommand
    ) {
        resetSimulatorPositionCommand.includeOnSmartDashboard("Reset Simulator Position");
        driveToOutpostCommand.includeOnSmartDashboard("Drive to Outpost");

    }

}
