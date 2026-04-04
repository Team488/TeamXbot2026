package competition;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;
import com.pathplanner.lib.commands.PathfindingCommand;
import com.pathplanner.lib.config.PIDConstants;
import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.controllers.PPHolonomicDriveController;
import competition.auto_programs.AimAndShootFromHereCommand;
import competition.subsystems.collector_intake.commands.CollectorIntakeCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.intake_deploy.commands.IntakeDeployExtendCommand;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

import javax.inject.Inject;

public class ConfigurePathPlannerLib {

    @Inject
    public ConfigurePathPlannerLib(PoseSubsystem pose, DriveSubsystem drive,
                                   IntakeDeployExtendCommand intakeDeployExtendCommand,
                                   CollectorIntakeCommand collectorIntakeCommand,
                                   AimAndShootFromHereCommand aimAndShootFromHereCommand) {
        NamedCommands.registerCommand("IntakeDeployExtend", intakeDeployExtendCommand);
        NamedCommands.registerCommand("CollectorIntake", collectorIntakeCommand);
        NamedCommands.registerCommand("AimAndShootFromHere", aimAndShootFromHereCommand);

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
            e.printStackTrace();
        }
    }
}
