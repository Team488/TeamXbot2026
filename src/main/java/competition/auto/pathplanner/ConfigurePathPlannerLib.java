package competition.auto.pathplanner;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;
import com.pathplanner.lib.commands.PathfindingCommand;
import com.pathplanner.lib.config.PIDConstants;
import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.controllers.PPHolonomicDriveController;

import competition.auto.commands.AimAndShootFromHereCommand;
import competition.command_groups.FireWhenShooterAndHoodReady;
import competition.command_groups.PrepareToShootCommandGroup;
import competition.command_groups.WaitForRotationAndHoodAndShooterToBeAtGoalCommandGroup;
import competition.subsystems.collector_intake.commands.CollectorIntakeCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.intake_deploy.commands.IntakeDeployExtendCommand;
import competition.subsystems.intake_deploy.commands.IntakeDeployExtendWithoutPidCommand;
import competition.subsystems.intake_deploy.commands.IntakeDeployRetractCommand;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.pose.TrajectoriesCalculation;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

import javax.inject.Inject;

public class ConfigurePathPlannerLib {

    private static final double manualIntakeDeployTimeoutSeconds = 1.5;

    @Inject
    public ConfigurePathPlannerLib(PoseSubsystem pose, DriveSubsystem drive,
                                   IntakeDeployExtendCommand intakeDeployExtendCommand,
                                   IntakeDeployRetractCommand intakeDeployRetractCommand,
                                   IntakeDeployExtendWithoutPidCommand intakeDeployManualExtendCommand,
                                   IntakeDeployExtendWithoutPidCommand intakeDeployManualRetractCommand,
                                   CollectorIntakeCommand collectorIntakeCommand,
                                   AimAndShootFromHereCommand aimAndShootFromHereCommand,
                                   PrepareToShootCommandGroup prepareToShootCommandGroup,
                                   WaitForRotationAndHoodAndShooterToBeAtGoalCommandGroup waitForRotationAndHoodAndShooterToBeAtGoalCommandGroup,
                                   FireWhenShooterAndHoodReady fireWhenShooterAndHoodReady
    ) {
        NamedCommands.registerCommand("IntakeDeployExtend", intakeDeployExtendCommand);
        NamedCommands.registerCommand("IntakeDeployRetract", intakeDeployRetractCommand);
        // Same as the operator holding the left stick fully down/up (the maintainer's manual control calls
        // setPower with the stick value). These run until cancelled, so use them as zoned event markers;
        // the timeout is only a safety net.
        NamedCommands.registerCommand("IntakeDeployManualExtend",
                intakeDeployManualExtendCommand.setPower(() -> -1.0).withTimeout(manualIntakeDeployTimeoutSeconds));
        NamedCommands.registerCommand("IntakeDeployManualRetract",
                intakeDeployManualRetractCommand.setPower(() -> 1.0).withTimeout(manualIntakeDeployTimeoutSeconds));
        NamedCommands.registerCommand("CollectorIntake", collectorIntakeCommand);
        NamedCommands.registerCommand("AimAndShootFromHere", aimAndShootFromHereCommand);
        NamedCommands.registerCommand("WaitForRotationAndHoodAndShooterToBeAtGoal", waitForRotationAndHoodAndShooterToBeAtGoalCommandGroup);
        // The path files' event markers use this longer name.
        NamedCommands.registerCommand("WaitForRotationAndHoodAndShooterToBeAtGoalCommandGroup", waitForRotationAndHoodAndShooterToBeAtGoalCommandGroup);

        NamedCommands.registerCommand("WarmupShooterNear", prepareToShootCommandGroup.setPresetLocation(TrajectoriesCalculation.PresetShootingDistance.NEAR));
        NamedCommands.registerCommand("FireWhenShooterAndHoodReady", fireWhenShooterAndHoodReady);

        try {
            AutoBuilder.configure(
                    pose::getCurrentPose2d,
                    pose::setCurrentPosition,
                    drive::getRobotRelativeSpeeds,
                    (speeds, feedforwards) -> drive.driveWithChassisSpeeds(speeds),
                    new PPHolonomicDriveController(
                            new PIDConstants(5.0, 0.0, 0.0),
                            new PIDConstants(5.0, 0.0, 0.0)
                    ),
                    RobotConfig.fromGUISettings(),
                    () -> {
                        var alliance = DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue);
                        return alliance == DriverStation.Alliance.Red;
                    },
                    drive
            );

            // Additionally warm up PathPlanner (not sure how necessary it is)
            CommandScheduler.getInstance().schedule(PathfindingCommand.warmupCommand());
            System.out.println("PathPlanner AutoBuilder configured and warmup scheduled.");
        } catch (Exception e) {
            System.out.println("PathPlannerLib failed to configure!");
            e.printStackTrace();
        }
    }
}
