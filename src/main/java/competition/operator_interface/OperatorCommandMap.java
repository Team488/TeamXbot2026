package competition.operator_interface;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import competition.auto_programs.vision.MoveAcrossFieldCommandGroup;
import competition.command_groups.FireWhenShooterReadyCommandGroup;
import competition.command_groups.HopperAndIntakeCommandGroup;
import competition.simulation.commands.ResetSimulatedPoseCommand;
import competition.subsystems.climber.ClimberSubsystem;
import competition.subsystems.climber.commands.ClimberSetPointCommand;
import competition.subsystems.drive.commands.DriveToOutpostCommand;
import competition.subsystems.climber.commands.ClimberExtendCommand;
import competition.subsystems.climber.commands.ClimberRetractCommand;
import competition.subsystems.drive.commands.DebugSwerveModuleCommand;
import competition.subsystems.drive.commands.SwerveDriveWithJoysticksCommand;
import competition.subsystems.fuel_intake.commands.FuelEjectCommand;
import competition.subsystems.fuel_intake.commands.FuelIntakeCommand;
import competition.subsystems.hood.commands.HoodExtendCommands;
import competition.subsystems.hood.commands.HoodRetractCommands;
import competition.subsystems.hood.commands.TrimHoodDownCommand;
import competition.subsystems.hood.commands.TrimHoodUpCommand;
import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import competition.subsystems.intake_deploy.commands.CalibrateOffsetDown;
import competition.subsystems.intake_deploy.commands.CalibrateOffsetUp;
import competition.subsystems.intake_deploy.commands.IntakeDeployExtendCommand;
import competition.subsystems.intake_deploy.commands.IntakeDeployRetractCommand;
import competition.subsystems.pose.Landmarks;
import competition.subsystems.shooter.commands.ShooterOutputCommand;
import competition.subsystems.shooter.commands.TrimShooterVelocityDown;
import competition.subsystems.shooter.commands.TrimShooterVelocityUp;
import competition.subsystems.shooter_feeder.commands.ShooterFeederFire;
import xbot.common.controls.sensors.XXboxController;
import xbot.common.subsystems.autonomous.SetAutonomousCommand;
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
            ShooterOutputCommand shooterOutputCommand,
            TrimShooterVelocityUp trimShooterVelocityUp,
            TrimShooterVelocityDown trimShooterVelocityDown
    ) {

    }

    @Inject
    public void setupDriveCommands(OperatorInterface operatorInterface,
                                   SetRobotHeadingCommand resetHeading,
                                   DebugSwerveModuleCommand debugModule,
                                   ChangeActiveSwerveModuleCommand changeActiveModule,
                                   SwerveDriveWithJoysticksCommand typicalSwerveDrive
    ) {
        resetHeading.setHeadingToApply(0);
        operatorInterface.driverGamepad.getifAvailable(XXboxController.XboxButton.Start).onTrue(resetHeading);

        operatorInterface.driverGamepad.getPovIfAvailable(0).onTrue(debugModule);
        operatorInterface.driverGamepad.getPovIfAvailable(90).onTrue(changeActiveModule);
        operatorInterface.driverGamepad.getPovIfAvailable(180).onTrue(typicalSwerveDrive);
    }
  
    @Inject
    public void setupAutoCommands(Provider<SetAutonomousCommand> setAutonomousCommandProvider,
                                  DriveToOutpostCommand driveToOutpostCommand,
                                  MoveAcrossFieldCommandGroup moveAcrossFieldCommand
    ) {
        driveToOutpostCommand.includeOnSmartDashboard("Drive to Outpost");

        var moveAcrossField = setAutonomousCommandProvider.get();
        moveAcrossField.setAutoCommand(moveAcrossFieldCommand, Landmarks.blueStartTrenchToOutpost);
        moveAcrossField.includeOnSmartDashboard("Move across field.");
    }

    @Inject
    public void setupOperatorGamepad(OperatorInterface operatorInterface,
                                     ShooterFeederFire shooterFeederFire,
                                     HopperRollerSubsystem hopperRollerSubsystem,
                                     HoodExtendCommands hoodExtend,
                                     HoodRetractCommands hoodRetract,
                                     IntakeDeployExtendCommand intakeDeployExtendCommand,
                                     IntakeDeployRetractCommand intakeDeployRetractCommand,
                                     CalibrateOffsetUp calibrateOffsetUp,
                                     HopperAndIntakeCommandGroup intakeCommand,
                                     FireWhenShooterReadyCommandGroup shooterOutputCommand
    ) {
        operatorInterface.operatorGamepad.getifAvailable(XXboxController.XboxButton.RightTrigger)
                    .whileTrue(hopperRollerSubsystem.getIntakeCommand().alongWith(shooterFeederFire));

        operatorInterface.operatorGamepad.getifAvailable(XXboxController.XboxButton.A).whileTrue(intakeCommand);
        operatorInterface.operatorGamepad.getifAvailable(XXboxController.XboxButton.X).whileTrue(hopperRollerSubsystem.getIntakeCommand());
        operatorInterface.operatorGamepad.getifAvailable(XXboxController.XboxButton.Y).whileTrue(intakeDeployExtendCommand);
        operatorInterface.operatorGamepad.getifAvailable(XXboxController.XboxButton.B).whileTrue(intakeDeployRetractCommand);

        operatorInterface.operatorGamepad.getifAvailable(XXboxController.XboxButton.LeftBumper).whileTrue(hoodExtend);
        operatorInterface.operatorGamepad.getifAvailable(XXboxController.XboxButton.RightBumper).whileTrue(hoodRetract);

        operatorInterface.operatorGamepad.getifAvailable(XXboxController.XboxButton.Start).onTrue(calibrateOffsetUp);

        operatorInterface.operatorGamepad.getPovIfAvailable(0).whileTrue(shooterOutputCommand);
    }

    @Inject
    public void setupDebugGamepad(OperatorInterface operatorInterface,
                                  ClimberExtendCommand climberExtendCommand,
                                  ClimberRetractCommand climberRetractCommand,

                                  ShooterOutputCommand shooterOutputCommand,
                                  TrimShooterVelocityUp trimShooterVelocityUp,
                                  TrimShooterVelocityDown trimShooterVelocityDown,
                                  FuelIntakeCommand fuelIntakeCommand,
                                  TrimHoodUpCommand trimHoodUpCommand,
                                  TrimHoodDownCommand trimHoodDownCommand,
                                  IntakeDeployExtendCommand intakeDeployExtendCommand,
                                  IntakeDeployRetractCommand intakeDeployRetractCommand,
                                  FuelEjectCommand fuelEjectCommand,
                                  ShooterFeederFire shooterFeederFire,
                                  HopperRollerSubsystem hopperRollerSubsystem,
                                  CalibrateOffsetDown calibrateOffsetDown,
                                  CalibrateOffsetUp calibrateOffsetUp,
                                  ClimberSubsystem climberSubsystem,
                                  ClimberSetPointCommand climberSetPointCommand

    ) {
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.LeftBumper).whileTrue(climberRetractCommand);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.RightBumper).whileTrue(climberExtendCommand);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.LeftTrigger).onTrue(trimShooterVelocityDown);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.RightTrigger).onTrue(trimShooterVelocityUp);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.X).whileTrue(shooterOutputCommand);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.Y).whileTrue(fuelIntakeCommand);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.A).onTrue(intakeDeployExtendCommand);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.B).onTrue(intakeDeployRetractCommand);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.Start).onTrue(calibrateOffsetUp);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.LeftStick).whileTrue(hopperRollerSubsystem.getEjectCommand());
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.RightStick).whileTrue(hopperRollerSubsystem.getIntakeCommand());
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.Back).onTrue(climberSetPointCommand);

        operatorInterface.setupDebugGamepad.getPovIfAvailable(0).onTrue(climberSubsystem.getCalibrateOffsetRetractCommand());
        operatorInterface.setupDebugGamepad.getPovIfAvailable(90).whileTrue(fuelEjectCommand);
        operatorInterface.setupDebugGamepad.getPovIfAvailable(180).onTrue(trimHoodUpCommand);
        operatorInterface.setupDebugGamepad.getPovIfAvailable(270).whileTrue(shooterFeederFire);
    }

    @Inject
    public void setupSimulatorCommands(
            ResetSimulatedPoseCommand resetPose
    ) {
        resetPose.includeOnSmartDashboard();
    }
}
