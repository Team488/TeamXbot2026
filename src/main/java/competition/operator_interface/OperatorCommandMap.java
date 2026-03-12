package competition.operator_interface;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import competition.auto_programs.ShootFromHubCommandGroup;
import competition.auto_programs.ShootFromTrenchCommandGroup;
import competition.auto_programs.vision.MoveAcrossFieldCommandGroup;
import competition.command_groups.FireWhenReadyAndRetractIntakeDeployCommandGroup;
import competition.command_groups.FireWhenReadyShooterCommandGroup;
import competition.command_groups.HopperAndIntakeCommandGroup;
import competition.command_groups.HopperAndIntakeEjectCommandGroup;
import competition.command_groups.PrepareToShootCommandGroup;
import competition.simulation.commands.ResetSimulatedPoseCommand;
import competition.subsystems.climber.ClimberSubsystem;
import competition.subsystems.climber.commands.ClimberSetPointCommand;
import competition.subsystems.collector_intake.commands.CollectorEjectCommand;
import competition.subsystems.collector_intake.commands.CollectorIntakeCommand;
import competition.subsystems.drive.commands.DebugSwerveModuleCommand;
import competition.subsystems.drive.commands.DriveToOutpostCommand;
import competition.subsystems.drive.commands.RotateToHubCommand;
import competition.subsystems.drive.commands.SwerveDriveWithJoysticksCommand;
import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.hood.commands.DropHoodForTrenchCommand;
import competition.subsystems.hood.commands.TrimHoodDownCommand;
import competition.subsystems.hood.commands.TrimHoodUpCommand;
import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import competition.subsystems.intake_deploy.commands.CalibrateOffsetDown;
import competition.subsystems.intake_deploy.commands.CalibrateOffsetUp;
import competition.subsystems.intake_deploy.commands.ForceIntakeDownToEndStopCommand;
import competition.subsystems.intake_deploy.commands.IntakeDeployExtendCommand;
import competition.subsystems.intake_deploy.commands.IntakeDeployRetractCommand;
import competition.subsystems.pose.Landmarks;
import competition.subsystems.pose.TrajectoriesCalculation;
import competition.subsystems.shooter.ShooterSubsystem;
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
    public OperatorCommandMap() {
    }

    // Example for setting up a command to fire when a button is pressed:
    @Inject
    public void setupOperatorCommands(
            OperatorInterface operatorInterface,
            ShooterOutputCommand shooterOutputCommand,
            TrimShooterVelocityUp trimShooterVelocityUp,
            TrimShooterVelocityDown trimShooterVelocityDown) {

    }

    @Inject
    public void setupDriveCommands(OperatorInterface operatorInterface,
            SetRobotHeadingCommand resetHeading,
            DebugSwerveModuleCommand debugModule,
            ChangeActiveSwerveModuleCommand changeActiveModule,
            SwerveDriveWithJoysticksCommand typicalSwerveDrive,
            Provider<ClimberSetPointCommand> climberSetPoint,
            ClimberSubsystem climber,
            DropHoodForTrenchCommand dropHoodForTrenchCommand,
            RotateToHubCommand rotateToHubCommand) {
        operatorInterface.driverGamepad.getifAvailable(XXboxController.XboxButton.Start).onTrue(resetHeading);
        operatorInterface.driverGamepad.getifAvailable(XXboxController.XboxButton.X)
                .whileTrue(dropHoodForTrenchCommand);
        operatorInterface.driverGamepad.getifAvailable(XXboxController.XboxButton.A).whileTrue(rotateToHubCommand);

        // Commenting out so it's not accidentally pressed during a match
        // operatorInterface.driverGamepad.getPovIfAvailable(0).onTrue(debugModule);
        // operatorInterface.driverGamepad.getPovIfAvailable(90).onTrue(changeActiveModule);
        // operatorInterface.driverGamepad.getPovIfAvailable(180).onTrue(typicalSwerveDrive);
    }

    @Inject
    public void setupOperatorGamepad(OperatorInterface operatorInterface,
            HoodSubsystem hoodSubsystem,
            ShooterSubsystem shooterSubsystem,
            IntakeDeployExtendCommand intakeDeployExtendCommand,
            IntakeDeployRetractCommand intakeDeployRetractCommand,
            ForceIntakeDownToEndStopCommand forceIntakeDownCommand,
            CalibrateOffsetUp calibrateIntakeOffsetUp,
            HopperAndIntakeCommandGroup intakeCommand,
            HopperAndIntakeEjectCommandGroup ejectCommand,
            FireWhenReadyShooterCommandGroup fireWhenReadyShooterCommandGroup,
            FireWhenReadyAndRetractIntakeDeployCommandGroup fireWhenReadyAndRetractIntakeDeployCommandGroup,
            Provider<PrepareToShootCommandGroup> prepareToShootCommand) {
        var prepareToShootNear = prepareToShootCommand.get()
                .setPresetLocation(TrajectoriesCalculation.PresetShootingDistance.NEAR);
        var prepareToShootTowerClose = prepareToShootCommand.get()
                .setPresetLocation(TrajectoriesCalculation.PresetShootingDistance.TOWER_FAR);
        var prepareToShootTrench = prepareToShootCommand.get()
                .setPresetLocation(TrajectoriesCalculation.PresetShootingDistance.TRENCH);
        var prepareToShootCorner = prepareToShootCommand.get()
                .setPresetLocation(TrajectoriesCalculation.PresetShootingDistance.CORNER);

        operatorInterface.operatorGamepad.getifAvailable(XXboxController.XboxButton.RightTrigger)
                .whileTrue(fireWhenReadyShooterCommandGroup);

        operatorInterface.operatorGamepad.getifAvailable(XXboxController.XboxButton.LeftBumper)
                .whileTrue(intakeDeployRetractCommand);
        operatorInterface.operatorGamepad.getifAvailable(XXboxController.XboxButton.RightBumper)
                .whileTrue(intakeDeployExtendCommand);

        operatorInterface.operatorGamepad.getifAvailable(XXboxController.XboxButton.LeftTrigger)
                .whileTrue(intakeCommand);

        operatorInterface.operatorGamepad.getifAvailable(XXboxController.XboxButton.X).whileTrue(prepareToShootNear);
        operatorInterface.operatorGamepad.getifAvailable(XXboxController.XboxButton.Y)
                .whileTrue(prepareToShootTowerClose);
        operatorInterface.operatorGamepad.getifAvailable(XXboxController.XboxButton.B).whileTrue(prepareToShootCorner);
        operatorInterface.operatorGamepad.getifAvailable(XXboxController.XboxButton.A).whileTrue(prepareToShootTrench);

        operatorInterface.operatorGamepad.getifAvailable(XXboxController.XboxButton.Back)
                .whileTrue(forceIntakeDownCommand);

        operatorInterface.operatorGamepad.getifAvailable(XXboxController.XboxButton.Start)
                .onTrue(calibrateIntakeOffsetUp);

        operatorInterface.operatorGamepad.getPovIfAvailable(180).whileTrue(ejectCommand);
        operatorInterface.operatorGamepad.getPovIfAvailable(0)
                .whileTrue(fireWhenReadyAndRetractIntakeDeployCommandGroup);
    }

    @Inject
    public void setupDebugGamepad(OperatorInterface operatorInterface,

            ShooterOutputCommand shooterOutputCommand,
            TrimShooterVelocityUp trimShooterVelocityUp,
            TrimShooterVelocityDown trimShooterVelocityDown,
            CollectorIntakeCommand collectorIntakeCommand,
            TrimHoodUpCommand trimHoodUpCommand,
            TrimHoodDownCommand trimHoodDownCommand,
            IntakeDeployExtendCommand intakeDeployExtendCommand,
            IntakeDeployRetractCommand intakeDeployRetractCommand,
            CollectorEjectCommand collectorEjectCommand,
            ShooterFeederFire shooterFeederFire,
            HopperRollerSubsystem hopperRollerSubsystem,
            CalibrateOffsetDown calibrateOffsetDown,
            CalibrateOffsetUp calibrateOffsetUp

    ) {
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.LeftTrigger)
                .onTrue(trimShooterVelocityDown);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.RightTrigger)
                .onTrue(trimShooterVelocityUp);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.X)
                .whileTrue(shooterOutputCommand);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.Y)
                .whileTrue(collectorIntakeCommand);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.A)
                .onTrue(intakeDeployExtendCommand);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.B)
                .onTrue(intakeDeployRetractCommand);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.Back).onTrue(calibrateOffsetDown);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.Start).onTrue(calibrateOffsetUp);
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.LeftStick)
                .whileTrue(hopperRollerSubsystem.getEjectCommand());
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.RightStick)
                .whileTrue(hopperRollerSubsystem.getIntakeCommand());
        operatorInterface.setupDebugGamepad.getPovIfAvailable(90).whileTrue(collectorEjectCommand);
        operatorInterface.setupDebugGamepad.getPovIfAvailable(180).onTrue(trimHoodUpCommand);
        operatorInterface.setupDebugGamepad.getPovIfAvailable(270).whileTrue(shooterFeederFire);
    }

    @Inject
    public void setupAutoCommands(Provider<SetAutonomousCommand> setAutonomousCommandProvider,
            DriveToOutpostCommand driveToOutpostCommand,
            MoveAcrossFieldCommandGroup moveAcrossFieldCommand,
            ShootFromTrenchCommandGroup shootFromTrenchCommandGroup,
            ShootFromHubCommandGroup shootFromHubCommandGroup) {
        driveToOutpostCommand.includeOnSmartDashboard("Drive to Outpost");

        var moveAcrossField = setAutonomousCommandProvider.get();
        moveAcrossField.setAutoCommand(moveAcrossFieldCommand, Landmarks.blueStartTrenchToOutpost);
        moveAcrossField.includeOnSmartDashboard("Move midway through field and back.");

        var shootFromTrench = setAutonomousCommandProvider.get();
        shootFromTrench.setAutoCommand(shootFromTrenchCommandGroup, Landmarks.blueStartTrenchToOutpost);
        shootFromTrench.includeOnSmartDashboard("Shoot from trench.");

        var shootFromHub = setAutonomousCommandProvider.get();
        shootFromHub.setAutoCommand(shootFromHubCommandGroup, Landmarks.blueStartTrenchToOutpost);
        shootFromHub.includeOnSmartDashboard("Shoot from hub.");
    }

    @Inject
    public void setupSimulatorCommands(
            ResetSimulatedPoseCommand resetPose) {
        resetPose.includeOnSmartDashboard();
    }
}
