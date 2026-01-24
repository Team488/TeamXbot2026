package competition.operator_interface;

import javax.inject.Inject;
import javax.inject.Singleton;

import competition.subsystems.drive.commands.DriveToOutpostCommand;
import competition.subsystems.fuel_intake.IntakeSubsystem;
import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.shooter.commands.ShooterOutputCommand;
import competition.subsystems.shooter.commands.TrimShooterVelocityDown;
import competition.subsystems.shooter.commands.TrimShooterVelocityUp;
import xbot.common.controls.sensors.XXboxController;
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
            SetRobotHeadingCommand resetHeading,
            ShooterOutputCommand shooterOutputCommand,
            TrimShooterVelocityUp trimShooterVelocityUp,
            TrimShooterVelocityDown trimShooterVelocityDown
    ) {
        resetHeading.setHeadingToApply(0);
        operatorInterface.gamepad.getifAvailable(1).onTrue(resetHeading);

        operatorInterface.debugGamepad.getifAvailable(XXboxController.XboxButton.A).whileTrue(shooterOutputCommand);
        operatorInterface.debugGamepad.getifAvailable(XXboxController.XboxButton.X).onTrue(trimShooterVelocityDown);
        operatorInterface.debugGamepad.getifAvailable(XXboxController.XboxButton.Y).onTrue(trimShooterVelocityUp);

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
