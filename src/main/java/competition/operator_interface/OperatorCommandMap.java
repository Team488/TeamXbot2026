package competition.operator_interface;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import competition.auto_programs.AimAndShootFromHereCommand;
import competition.auto_programs.ppl.LeftBumpAutoCommand;
import competition.auto_programs.ShootFromHubCommandGroup;
import competition.auto_programs.ShootFromTrenchCommandGroup;
import competition.auto_programs.CollectAndShootTwiceCommand;
import competition.auto_programs.ppl.RightBumpAutoCommand;
import competition.auto_programs.vision.JustDriveNeutralMoveCommand;
import competition.auto_programs.vision.MoveAcrossFieldCommandGroup;
import competition.auto_programs.vision.ShootFromTrenchThenMoveToNeutralCommand;
import competition.command_groups.vision.DriveThroughAllianceTrenchCommand;
import competition.command_groups.HopperAndIntakeCommandGroup;
import competition.command_groups.HopperAndIntakeEjectCommandGroup;
import competition.command_groups.IntakeSlowlyAndFireWhenReady;
import competition.command_groups.PrepareToShootCommandGroup;
import competition.simulation.commands.ResetSimulatedPoseCommand;
import competition.subsystems.collector_intake.commands.CollectorEjectCommand;
import competition.subsystems.collector_intake.commands.CollectorIntakeCommand;
import competition.subsystems.drive.commands.DriveToOutpostCommand;
import competition.subsystems.drive.commands.PrecisionModeCommand;
import competition.subsystems.drive.commands.RobotRelativeForwardBackCommand;
import competition.subsystems.drive.commands.RotateToHubCommand;
import competition.subsystems.drive.commands.XPositionCommand;
import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.hood.commands.DropHoodForTrenchCommand;
import competition.subsystems.hood.commands.HoodToZeroCommand;
import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import competition.subsystems.intake_deploy.commands.IntakeDeployExtendCommand;
import competition.subsystems.intake_deploy.commands.IntakeDeployOscillating;
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
                                   DropHoodForTrenchCommand dropHoodForTrenchCommand,
                                   RotateToHubCommand rotateToHubCommand,
                                   XPositionCommand xPositionCommand,
                                   DriveThroughAllianceTrenchCommand driveThroughAllianceTrenchCommand,
                                   IntakeSlowlyAndFireWhenReady intakeSlowlyAndFireWhenReady,
                                   PrecisionModeCommand precisionModeCommand,
                                   AimAndShootFromHereCommand aimAndShootFromHereCommand,
                                   RobotRelativeForwardBackCommand robotRelativeForwardBackCommand
    ) {
        operatorInterface.driverGamepad.getPovIfAvailable(0).onTrue(driveThroughAllianceTrenchCommand);
        // operatorInterface.driverGamepad.getPovIfAvailable(180).onTrue(lowPowerModeOffCommand);
        operatorInterface.driverGamepad.getifAvailable(XXboxController.XboxButton.Start).onTrue(resetHeading);
        operatorInterface.driverGamepad.getifAvailable(XXboxController.XboxButton.X).whileTrue(xPositionCommand);
        operatorInterface.driverGamepad.getifAvailable(XXboxController.XboxButton.A).whileTrue(rotateToHubCommand);
        operatorInterface.driverGamepad.getifAvailable(XXboxController.XboxButton.RightBumper).whileTrue(intakeSlowlyAndFireWhenReady);
        operatorInterface.driverGamepad.getifAvailable(XXboxController.XboxButton.Y).whileTrue(precisionModeCommand);
        operatorInterface.driverGamepad.getifAvailable(XXboxController.XboxButton.B).whileTrue(aimAndShootFromHereCommand);
        operatorInterface.driverGamepad.getifAvailable(XXboxController.XboxButton.Back).whileTrue(robotRelativeForwardBackCommand);

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
                                     HopperAndIntakeCommandGroup intakeCommand,
                                     HopperAndIntakeEjectCommandGroup ejectCommand,
                                     IntakeSlowlyAndFireWhenReady  intakeSlowlyAndFireWhenReady,
                                     Provider<PrepareToShootCommandGroup> prepareToShootCommand,
                                     Provider<HoodToZeroCommand> hoodToZeroCommandProvider) {
        var prepareToShootNear = prepareToShootCommand.get()
                .setPresetLocation(TrajectoriesCalculation.PresetShootingDistance.NEAR);
        var prepareToShootTowerClose = prepareToShootCommand.get()
                .setPresetLocation(TrajectoriesCalculation.PresetShootingDistance.TOWER_FAR);
        var prepareToShootTrench = prepareToShootCommand.get()
                .setPresetLocation(TrajectoriesCalculation.PresetShootingDistance.TRENCH);
        var prepareToShootCorner = prepareToShootCommand.get()
                .setPresetLocation(TrajectoriesCalculation.PresetShootingDistance.CORNER);

        operatorInterface.operatorGamepad.getifAvailable(XXboxController.XboxButton.RightTrigger)
                .whileTrue(intakeSlowlyAndFireWhenReady);

        operatorInterface.operatorGamepad.getifAvailable(XXboxController.XboxButton.LeftBumper)
                .whileTrue(intakeDeployRetractCommand);
        operatorInterface.operatorGamepad.getifAvailable(XXboxController.XboxButton.RightBumper)
                .whileTrue(intakeDeployExtendCommand);

        operatorInterface.operatorGamepad.getifAvailable(XXboxController.XboxButton.LeftTrigger)
                .whileTrue(intakeCommand);

        operatorInterface.operatorGamepad.getifAvailable(XXboxController.XboxButton.X)
                .whileTrue(prepareToShootNear)
                .onFalse(hoodToZeroCommandProvider.get());
        operatorInterface.operatorGamepad.getifAvailable(XXboxController.XboxButton.Y)
                .whileTrue(prepareToShootTrench)
                .onFalse(hoodToZeroCommandProvider.get());
        operatorInterface.operatorGamepad.getifAvailable(XXboxController.XboxButton.B)
                .whileTrue(prepareToShootCorner)
                .onFalse(hoodToZeroCommandProvider.get());
        operatorInterface.operatorGamepad.getifAvailable(XXboxController.XboxButton.A)
                .whileTrue(prepareToShootTowerClose)
                .onFalse(hoodToZeroCommandProvider.get());

        operatorInterface.operatorGamepad.getPovIfAvailable(180).whileTrue(ejectCommand);
    }

    @Inject
    public void setupDebugGamepad(OperatorInterface operatorInterface,
                                  ShooterOutputCommand shooterOutputCommand,
                                  TrimShooterVelocityUp trimShooterVelocityUp,
                                  TrimShooterVelocityDown trimShooterVelocityDown,
                                  CollectorIntakeCommand collectorIntakeCommand,
                                  IntakeDeployExtendCommand intakeDeployExtendCommand,
                                  IntakeDeployRetractCommand intakeDeployRetractCommand,
                                  CollectorEjectCommand collectorEjectCommand,
                                  ShooterFeederFire shooterFeederFire,
                                  HopperRollerSubsystem hopperRollerSubsystem
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
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.LeftStick)
                .whileTrue(hopperRollerSubsystem.getEjectCommand());
        operatorInterface.setupDebugGamepad.getifAvailable(XXboxController.XboxButton.RightStick)
                .whileTrue(hopperRollerSubsystem.getIntakeCommand());
        operatorInterface.setupDebugGamepad.getPovIfAvailable(90).whileTrue(collectorEjectCommand);
        operatorInterface.setupDebugGamepad.getPovIfAvailable(270).whileTrue(shooterFeederFire);
    }

    @Inject
    public void setupAutoCommands(Provider<SetAutonomousCommand> setAutonomousCommandProvider,
                                  DriveToOutpostCommand driveToOutpostCommand,
                                  MoveAcrossFieldCommandGroup moveAcrossFieldCommand,
                                  ShootFromTrenchThenMoveToNeutralCommand shootFromTrenchThenMoveToNeutralCommand,
                                  ShootFromTrenchCommandGroup shootFromTrenchCommandGroup,
                                  ShootFromHubCommandGroup shootFromHubCommandGroup,
                                  JustDriveNeutralMoveCommand justDriveNeutralMoveCommand,
                                  CollectAndShootTwiceCommand collectAndShootTwiceCommand,
                                  LeftBumpAutoCommand leftBumpAutoCommand,
                                  RightBumpAutoCommand rightBumpAutoCommand
        ) {
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

        var moveAcrossField2Right = setAutonomousCommandProvider.get();
        moveAcrossField2Right.setAutoCommand(rightBumpAutoCommand, Landmarks.blueStartTrenchToOutpost);
        moveAcrossField2Right.includeOnSmartDashboard("Right bump double cycle.");

        var moveAcrossField2Left = setAutonomousCommandProvider.get();
        moveAcrossField2Left.setAutoCommand(leftBumpAutoCommand, Landmarks.blueStartTrenchToOutpost);
        moveAcrossField2Left.includeOnSmartDashboard("Left bump double cycle.");

        var shootFromTrenchThenMove = setAutonomousCommandProvider.get();
        shootFromTrenchThenMove.setAutoCommand(shootFromTrenchThenMoveToNeutralCommand, Landmarks.blueStartTrenchToOutpost);
        shootFromTrenchThenMove.includeOnSmartDashboard("Shoot from trench then collect from neutral zone.");

        var justDrivePortionOfAuto = setAutonomousCommandProvider.get();
        justDrivePortionOfAuto.setAutoCommand(justDriveNeutralMoveCommand, Landmarks.blueStartTrenchToOutpost);
        justDrivePortionOfAuto.includeOnSmartDashboard("Just drive Portion of Auto.");

        var collectAndShootTwice = setAutonomousCommandProvider.get();
        collectAndShootTwice.setAutoCommand(collectAndShootTwiceCommand, Landmarks.blueStartTrenchToOutpost);
        collectAndShootTwice.includeOnSmartDashboard("Collect and shoot twice.");
    }

    @Inject
    public void setupSimulatorCommands(
            ResetSimulatedPoseCommand resetPose) {
        resetPose.includeOnSmartDashboard();
    }

    @Inject
    public void setupTestingCommands(AimAndShootFromHereCommand aimAndShootFromHereCommand,
                                     DriveThroughAllianceTrenchCommand driveThroughAllianceTrenchCommand, IntakeDeployOscillating intakeDeployOscillating) {
        aimAndShootFromHereCommand.includeOnSmartDashboard();
        driveThroughAllianceTrenchCommand.includeOnSmartDashboard();
        intakeDeployOscillating.includeOnSmartDashboard();
    }
}