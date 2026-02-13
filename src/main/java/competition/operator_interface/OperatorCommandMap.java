package competition.operator_interface;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import competition.subsystems.climber.commands.ClimberExtendCommand;
import competition.subsystems.climber.commands.ClimberRetractCommand;
import competition.subsystems.drive.commands.DebugSwerveModuleCommand;
import competition.subsystems.drive.commands.SwerveDriveWithJoysticksCommand;
import competition.subsystems.fuel_intake.commands.FuelIntakeCommand;
import competition.subsystems.hood.commands.HoodToGoalCommand;
import competition.subsystems.hood.commands.HoodToZeroCommand;
import competition.subsystems.intake_deploy.commands.IntakeDeployExtendCommand;
import competition.subsystems.intake_deploy.commands.IntakeDeployRetractCommand;
import competition.subsystems.shooter.commands.ShooterStopCommand;
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
    public void setupOperatorCommands(
            OperatorInterface operatorInterface,
//            Provider<ShooterStopCommand> c,
            TrimShooterVelocityUp trimShooterVelocityUp,
            TrimShooterVelocityDown trimShooterVelocityDown
    ) {
//        var myCommand = c.get();
//        myCommand.setTargetVelocity(90);
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
    public void setupDebugGamepad(OperatorInterface operatorInterface,
                                     ClimberExtendCommand climberExtendCommand,
                                     ClimberRetractCommand climberRetractCommand,
                                     ShooterOutputCommand shooterOutputCommand,
                                     TrimShooterVelocityUp trimShooterVelocityUp,
                                     TrimShooterVelocityDown trimShooterVelocityDown,
                                     FuelIntakeCommand fuelIntakeCommand,
                                     HoodToGoalCommand hoodToGoalCommand,
                                     HoodToZeroCommand hoodToZeroCommand,
                                     IntakeDeployExtendCommand intakeDeployExtendCommand,
                                     IntakeDeployRetractCommand intakeDeployRetractCommand
    ) {
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.LeftBumper).whileTrue(climberExtendCommand);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.RightBumper).whileTrue(climberRetractCommand);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.LeftTrigger).whileTrue(trimShooterVelocityUp);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.RightTrigger).whileTrue(trimShooterVelocityDown);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.X).whileTrue(shooterOutputCommand);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.Y).onTrue(fuelIntakeCommand);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.A).whileTrue(intakeDeployExtendCommand);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.B).whileTrue(intakeDeployRetractCommand);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.RightJoystickYAxisPositive).whileTrue(hoodToGoalCommand);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.RightJoystickYAxisNegative).whileTrue(hoodToZeroCommand);
    }



}
