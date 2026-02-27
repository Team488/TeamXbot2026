package competition.command_groups;

import competition.subsystems.climber.commands.ClimberExtendCommand;
import competition.subsystems.climber.commands.ClimberRetractCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.intake_deploy.commands.IntakeDeployRetractCommand;
import competition.subsystems.pose.Landmarks;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import xbot.common.command.BaseSequentialCommandGroup;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;

public class VisionOutpostClimbCommandGroup extends BaseSequentialCommandGroup {
//    private final AprilTagFieldLayout aprilTagFieldLayout;
    @Inject
    public VisionOutpostClimbCommandGroup(Provider<SwerveSimpleTrajectoryCommand> trajectoryProvider,
                                          DriveSubsystem drive,
                                          AprilTagFieldLayout aprilTagFieldLayout,
                                          ClimberExtendCommand climberExtendCommand,
                                          ClimberRetractCommand climberRetractCommand,
                                          IntakeDeployRetractCommand intakeDeployRetractCommand
//                                          Landmarks landmarks, AprilTagFieldLayout aprilTagFieldLayout1
    )
    {
//        this.aprilTagFieldLayout = aprilTagFieldLayout;

        var readyOutpostClimb =  trajectoryProvider.get();

        ArrayList<XbotSwervePoint> readyOutpostPoint = new ArrayList<>();

        Pose2d readyOutpostClimbPose = PoseSubsystem.convertBlueToRedIfNeeded(Landmarks.blueVisionClimbReadyPose);

//        Pose2d readyOutpostClimbPose = PoseSubsystem.convertBlueToRedIfNeeded(Landmarks.getAllianceHubPose(
//                aprilTagFieldLayout, DriverStation.Alliance.Blue).minus(new Pose2d(1.8,1.8, Rotation2d.fromRotations(0)));

        readyOutpostPoint.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                readyOutpostClimbPose,2));

        readyOutpostClimb.logic.setKeyPoints(readyOutpostPoint);
        readyOutpostClimb.logic.setConstantVelocity(drive.getMaxTargetSpeedMetersPerSecond());


        var outpostClimb = trajectoryProvider.get();

        ArrayList<XbotSwervePoint> outpostPoint = new ArrayList<>();

        Pose2d outpostClimbPose = PoseSubsystem.convertBlueToRedIfNeeded(Landmarks.blueClimbMiddleOutpostSide);

        outpostPoint.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint
                (outpostClimbPose,2));

        outpostClimb.logic.setKeyPoints(outpostPoint);
        outpostClimb.logic.setConstantVelocity(drive.getMaxTargetSpeedMetersPerSecond());

        addCommands(
                readyOutpostClimb,
//                new ParallelCommandGroup(
//                        intakeDeployRetractCommand,
//                        climberExtendCommand
//                ),
                outpostClimb,
                climberRetractCommand
        );
    }
}
