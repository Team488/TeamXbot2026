package competition.command_groups;

import competition.subsystems.climber.commands.ClimberExtendCommand;
import competition.subsystems.climber.commands.ClimberRetractCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.intake_deploy.commands.IntakeDeployRetractCommand;
import competition.subsystems.pose.Landmarks;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import xbot.common.command.BaseSequentialCommandGroup;
import xbot.common.logging.RobotAssertionManager;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.SwervePointKinematics;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryMode;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.subsystems.oracle.SwervePointPathPlanning;
import xbot.common.subsystems.pose.BasePoseSubsystem;
import xbot.common.subsystems.pose.GameField;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VisionOutpostClimbCommandGroup extends BaseSequentialCommandGroup {
    private final DriveSubsystem drive;
    private final PoseSubsystem pose;
    private final SwervePointPathPlanning pathPlanning;
    private final GameField gamefield;


    @Inject
    public VisionOutpostClimbCommandGroup(Provider<SwerveSimpleTrajectoryCommand> trajectoryProvider,
                                          DriveSubsystem drive, PoseSubsystem pose,
                                          AprilTagFieldLayout aprilTagFieldLayout,
                                          ClimberExtendCommand climberExtendCommand,
                                          ClimberRetractCommand climberRetractCommand,
                                          IntakeDeployRetractCommand intakeDeployRetractCommand,
                                          SwervePointPathPlanning pathPlanning,
                                          PropertyFactory pf, HeadingModule.HeadingModuleFactory headingModuleFactory,
                                          RobotAssertionManager robotAssertionManager, GameField gamefield
    )
    {
        this.drive = drive;
        this.pose = pose;
        this.pathPlanning = pathPlanning;
        this.gamefield = gamefield;

        var drivePath = trajectoryProvider.get();



        var currentPose = pose.getCurrentPose2d();
        var finalPose = PoseSubsystem.convertBlueToRedIfNeeded(Landmarks.blueClimbMiddleOutpostSide);

        List<XbotSwervePoint> swervePoints = this.pathPlanning.generateSwervePoints(currentPose,finalPose,false);


        drivePath.logic.setPrioritizeRotationIfCloseToGoal(true);
        drivePath.logic.setVelocityMode(SwerveSimpleTrajectoryMode.GlobalKinematicsValue);
        drivePath.logic.setGlobalKinematicValues(
                new SwervePointKinematics(this.drive.getMaxAccelerationMetersPerSecondSquared(), 0, 0,
                        this.drive.getMaxAutoTargetSpeedMetersPerSecond()));

        drivePath.logic.setKeyPoints(swervePoints);



        addCommands(
                new ParallelCommandGroup(
                        intakeDeployRetractCommand,
                        climberExtendCommand
                ),
                climberRetractCommand
        );
    }
}
