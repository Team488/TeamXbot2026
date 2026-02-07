package competition.operator_interface;

import javax.inject.Inject;
import javax.inject.Singleton;

import competition.subsystems.climber.commands.ClimberExtendCommand;
import competition.subsystems.climber.commands.ClimberRetractCommand;
import competition.subsystems.drive.commands.DebugSwerveModuleCommand;
import competition.subsystems.drive.commands.SwerveDriveWithJoysticksCommand;
import competition.subsystems.fuel_intake.commands.FuelEjectCommand;
import competition.subsystems.fuel_intake.commands.FuelIntakeCommand;
import competition.subsystems.hood.commands.CloseHoodCommand;
import competition.subsystems.hood.commands.OpenHoodCommand;
import competition.subsystems.intake_deploy.commands.IntakeDeployExtendCommand;
import competition.subsystems.intake_deploy.commands.IntakeDeployRetractCommand;
import competition.subsystems.shooter.commands.ShooterOutputCommand;
import competition.subsystems.shooter.commands.TrimShooterVelocityDown;
import competition.subsystems.shooter.commands.TrimShooterVelocityUp;
import xbot.common.controls.sensors.XXboxController;
import xbot.common.subsystems.drive.swerve.commands.ChangeActiveSwerveModuleCommand;
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
            ShooterOutputCommand shooterOutputCommand,
            TrimShooterVelocityUp trimShooterVelocityUp,
            TrimShooterVelocityDown trimShooterVelocityDown
    ) {
        operatorInterface.debugGamepad.getifAvailable(XXboxController.XboxButton.A).whileTrue(shooterOutputCommand);
        operatorInterface.debugGamepad.getifAvailable(XXboxController.XboxButton.X).onTrue(trimShooterVelocityDown);
        operatorInterface.debugGamepad.getifAvailable(XXboxController.XboxButton.Y).onTrue(trimShooterVelocityUp);
    }

    @Inject
    public void setupDriveCommands(OperatorInterface operatorInterface,
                                   SetRobotHeadingCommand resetHeading,
                                   DebugSwerveModuleCommand debugModule,
                                   ChangeActiveSwerveModuleCommand changeActiveModule,
                                   SwerveDriveWithJoysticksCommand typicalSwerveDrive) {
        resetHeading.setHeadingToApply(0);
        operatorInterface.driverGamepad.getifAvailable(1).onTrue(resetHeading);

        operatorInterface.driverGamepad.getPovIfAvailable(0).onTrue(debugModule);
        operatorInterface.driverGamepad.getPovIfAvailable(90).onTrue(changeActiveModule);
        operatorInterface.driverGamepad.getPovIfAvailable(180).onTrue(typicalSwerveDrive);
    }
    @Inject
    public void victorsDebugCommandsForSubsystems(OperatorInterface operatorInterface,
                                     ClimberExtendCommand climberExtendCommand,
                                     ClimberRetractCommand climberRetractCommand,
                                     ShooterOutputCommand shooterOutputCommand,
                                     TrimShooterVelocityUp trimShooterVelocityUp,
                                     TrimShooterVelocityDown trimShooterVelocityDown,
                                     FuelIntakeCommand fuelIntakeCommand,
                                     OpenHoodCommand openHoodCommand,
                                     CloseHoodCommand closeHoodCommand,
                                     IntakeDeployExtendCommand intakeDeployExtendCommand,
                                     IntakeDeployRetractCommand intakeDeployRetractCommand
    ) {
        operatorInterface.victorSubsystemTestingGamepad.getifAvailable(XXboxController.XboxButton.LeftBumper).whileTrue(climberExtendCommand);
        operatorInterface.victorSubsystemTestingGamepad.getifAvailable(XXboxController.XboxButton.RightBumper).whileTrue(climberRetractCommand);
        operatorInterface.victorSubsystemTestingGamepad.getifAvailable(XXboxController.XboxButton.LeftTrigger).whileTrue(trimShooterVelocityUp);
        operatorInterface.victorSubsystemTestingGamepad.getifAvailable(XXboxController.XboxButton.RightTrigger).whileTrue(trimShooterVelocityDown);
        operatorInterface.victorSubsystemTestingGamepad.getifAvailable(XXboxController.XboxButton.X).whileTrue(shooterOutputCommand);
        operatorInterface.victorSubsystemTestingGamepad.getifAvailable(XXboxController.XboxButton.Y).onTrue(fuelIntakeCommand);
        operatorInterface.victorSubsystemTestingGamepad.getifAvailable(XXboxController.XboxButton.A).whileTrue(intakeDeployExtendCommand);
        operatorInterface.victorSubsystemTestingGamepad.getifAvailable(XXboxController.XboxButton.B).whileTrue(intakeDeployRetractCommand);
        operatorInterface.victorSubsystemTestingGamepad.getifAvailable(XXboxController.XboxButton.RightJoystickYAxisPositive).whileTrue(openHoodCommand);
        operatorInterface.victorSubsystemTestingGamepad.getifAvailable(XXboxController.XboxButton.RightJoystickYAxisNegative).whileTrue(closeHoodCommand);
    }



}
