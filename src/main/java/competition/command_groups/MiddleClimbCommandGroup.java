package competition.command_groups;

import competition.subsystems.climber.commands.ClimberExtendCommand;
import competition.subsystems.climber.commands.ClimberRetractCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.intake_deploy.commands.IntakeDeployRetractCommand;
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

        SwerveSimpleTrajectoryCommand readyMiddleClimb =  trajectoryProvider.get();
        SwerveSimpleTrajectoryCommand middleClimb = trajectoryProvider.get();

        ArrayList<XbotSwervePoint> readyMiddlePoint = new ArrayList<>();
        ArrayList<XbotSwervePoint> middlePoint = new ArrayList<>();

        Pose2d readyMiddleClimbPose = PoseSubsystem.convertBlueToRedIfNeeded(blueMiddleClimbReadyPose);
        Pose2d middleClimbPose = PoseSubsystem.convertBlueToRedIfNeeded(blueClimbCenter);

        readyMiddlePoint.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                readyMiddleClimbPose,4));

        middlePoint.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint
                (middleClimbPose,4));

        readyMiddleClimb.logic.setKeyPoints(readyMiddlePoint);
        readyMiddleClimb.logic.setConstantVelocity(drive.getMaxTargetSpeedMetersPerSecond());

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
