package competition.command_groups;

import competition.subsystems.climber.commands.ClimberExtendCommand;
import competition.subsystems.climber.commands.ClimberRetractCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.intake_deploy.commands.IntakeDeployRetractCommand;
import competition.subsystems.pose.Landmarks;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import xbot.common.command.BaseSequentialCommandGroup;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;

import static competition.subsystems.pose.Landmarks.blueClimbCenter;
import static competition.subsystems.pose.Landmarks.blueMiddleClimbReadyPose;

public class MiddleClimbCommandGroup extends BaseSequentialCommandGroup {

    @Inject
    public MiddleClimbCommandGroup(Provider<SwerveSimpleTrajectoryCommand> trajectoryProvider,
                                   DriveSubsystem drive,
                                   ClimberExtendCommand climberExtendCommand,
                                   ClimberRetractCommand climberRetractCommand,
                                   IntakeDeployRetractCommand intakeDeployRetractCommand) {

        var readyMiddleClimb =  trajectoryProvider.get();

        ArrayList<XbotSwervePoint> readyMiddlePoint = new ArrayList<>();

        Pose2d readyMiddleClimbPose = PoseSubsystem.convertBlueToRedIfNeeded(Landmarks.blueMiddleClimbReadyPose);

        readyMiddlePoint.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                readyMiddleClimbPose,4));

        readyMiddleClimb.logic.setKeyPoints(readyMiddlePoint);
        readyMiddleClimb.logic.setConstantVelocity(drive.getMaxTargetSpeedMetersPerSecond());


        var middleClimb = trajectoryProvider.get();

        ArrayList<XbotSwervePoint> middlePoint = new ArrayList<>();

        Pose2d middleClimbPose = PoseSubsystem.convertBlueToRedIfNeeded(Landmarks.blueClimbCenter);

        middlePoint.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint
                (middleClimbPose,4));

        middleClimb.logic.setKeyPoints(middlePoint);
        middleClimb.logic.setConstantVelocity(drive.getMaxTargetSpeedMetersPerSecond());

        addCommands(
                readyMiddleClimb,
                new ParallelCommandGroup(
                        intakeDeployRetractCommand,
                        climberExtendCommand
                ),
                middleClimb,
                climberRetractCommand
        );
    }
}
