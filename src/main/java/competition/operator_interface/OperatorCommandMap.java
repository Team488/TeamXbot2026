package competition.operator_interface;

import javax.inject.Inject;
import javax.inject.Singleton;

import competition.simulation.commands.ResetSimulatedPoseCommand;
import competition.subsystems.drive.commands.DriveToOutpostCommand;
import competition.subsystems.fuel_intake.IntakeSubsystem;
import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.climber.commands.ClimberExtendCommand;
import competition.subsystems.climber.commands.ClimberRetractCommand;
import competition.subsystems.drive.commands.DebugSwerveModuleCommand;
import competition.subsystems.drive.commands.SwerveDriveWithJoysticksCommand;
import competition.subsystems.fuel_intake.commands.FuelEjectCommand;
import competition.subsystems.fuel_intake.commands.FuelIntakeCommand;
import competition.subsystems.hood.commands.TrimHoodDownCommand;
import competition.subsystems.hood.commands.TrimHoodUpCommand;
import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import competition.subsystems.intake_deploy.commands.CalibrateOffsetDown;
import competition.subsystems.intake_deploy.commands.CalibrateOffsetUp;
import competition.subsystems.intake_deploy.commands.IntakeDeployExtendCommand;
import competition.subsystems.intake_deploy.commands.IntakeDeployRetractCommand;
import competition.subsystems.shooter.commands.ShooterOutputCommand;
import competition.subsystems.shooter.commands.TrimShooterVelocityDown;
import competition.subsystems.shooter.commands.TrimShooterVelocityUp;
import competition.subsystems.shooter_feeder.commands.ShooterFeederEject;
import xbot.common.controls.sensors.XXboxController;
import xbot.common.simulation.ResetSimulatorPositionCommand;
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
                                   SwerveDriveWithJoysticksCommand typicalSwerveDrive) {
        resetHeading.setHeadingToApply(0);
        operatorInterface.driverGamepad.getifAvailable(1).onTrue(resetHeading);

        operatorInterface.driverGamepad.getPovIfAvailable(0).onTrue(debugModule);
        operatorInterface.driverGamepad.getPovIfAvailable(90).onTrue(changeActiveModule);
        operatorInterface.driverGamepad.getPovIfAvailable(180).onTrue(typicalSwerveDrive);
    }
  
    @Inject
    public void setupAutoCommands(
            ResetSimulatedPoseCommand resetSimulatorPositionCommand,
            DriveToOutpostCommand driveToOutpostCommand
    ) {
        resetSimulatorPositionCommand.includeOnSmartDashboard("Reset Simulator Position");
        driveToOutpostCommand.includeOnSmartDashboard("Drive to Outpost");

    }
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
                                     ShooterFeederEject shooterFeederEject,
                                     HopperRollerSubsystem hopperRollerSubsystem,
                                     CalibrateOffsetDown calibrateOffsetDown,
                                     CalibrateOffsetUp calibrateOffsetUp
    ) {
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.LeftBumper).whileTrue(climberRetractCommand);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.RightBumper).whileTrue(climberExtendCommand);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.LeftTrigger).onTrue(trimShooterVelocityDown);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.RightTrigger).onTrue(trimShooterVelocityUp);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.X).whileTrue(shooterOutputCommand);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.Y).whileTrue(fuelIntakeCommand);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.A).whileTrue(intakeDeployExtendCommand);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.B).whileTrue(intakeDeployRetractCommand);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.Back).whileTrue(calibrateOffsetDown);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.Start).whileTrue(calibrateOffsetUp);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.LeftStick).whileTrue(hopperRollerSubsystem.getEjectCommand());
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.RightStick).whileTrue(hopperRollerSubsystem.getIntakeCommand());

        operatorInterface.setupDebugGamepad.getPovIfAvailable(0).onTrue(trimHoodDownCommand);
        operatorInterface.setupDebugGamepad.getPovIfAvailable(90).whileTrue(fuelEjectCommand);
        operatorInterface.setupDebugGamepad.getPovIfAvailable(180).onTrue(trimHoodUpCommand);
        operatorInterface.setupDebugGamepad.getPovIfAvailable(270).onTrue(shooterFeederEject);


    }



}
