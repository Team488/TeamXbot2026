package competition.command_groups;

import competition.subsystems.climber.commands.ClimberExtendCommand;
import competition.subsystems.climber.commands.ClimberRetractCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.OutpostSideClimbAutoCommand;
import competition.subsystems.drive.commands.ReadyOutpostSideClimbAutoCommand;
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
import static competition.subsystems.pose.Landmarks.blueClimbMiddleOutpostSide;
import static competition.subsystems.pose.Landmarks.blueOutpostSideClimbReadyPose;

public class OutpostClimbCommandGroup extends BaseSequentialCommandGroup {

    @Inject
    public OutpostClimbCommandGroup(Provider<SwerveSimpleTrajectoryCommand> trajectoryProvider,
                                    DriveSubsystem drive,
                                    OutpostSideClimbAutoCommand outpostSideClimbAutoCommand,
                                    ReadyOutpostSideClimbAutoCommand readyOutpostSideClimbAutoCommand,
                                    ClimberExtendCommand climberExtendCommand,
                                    ClimberRetractCommand climberRetractCommand,
                                    IntakeDeployRetractCommand intakeDeployRetractCommand) {
        SwerveSimpleTrajectoryCommand readyOutpostClimb =  trajectoryProvider.get();
        SwerveSimpleTrajectoryCommand outpostClimb = trajectoryProvider.get();

        ArrayList<XbotSwervePoint> readyOutpostPoint = new ArrayList<>();
        ArrayList<XbotSwervePoint> outpostPoint = new ArrayList<>();

        Pose2d readyOutpostClimbPose = PoseSubsystem.convertBlueToRedIfNeeded(blueOutpostSideClimbReadyPose);
        Pose2d outpostClimbPose = PoseSubsystem.convertBlueToRedIfNeeded(blueClimbMiddleOutpostSide);

        readyOutpostPoint.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                readyOutpostClimbPose,4));

        outpostPoint.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint
                (outpostClimbPose,4));

        readyOutpostClimb.logic.setKeyPoints(readyOutpostPoint);
        readyOutpostClimb.logic.setConstantVelocity(drive.getMaxTargetSpeedMetersPerSecond());

        outpostClimb.logic.setKeyPoints(outpostPoint);
        outpostClimb.logic.setConstantVelocity(drive.getMaxTargetSpeedMetersPerSecond());

        addCommands(
                readyOutpostClimb,
                new ParallelCommandGroup(
                        intakeDeployRetractCommand,
                        climberExtendCommand
                ),
                outpostClimb,
                climberRetractCommand
        );
    }
}
