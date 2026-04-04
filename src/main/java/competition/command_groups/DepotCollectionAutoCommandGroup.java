package competition.command_groups;

import competition.auto_programs.AimAndShootFromHereCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import competition.subsystems.intake_deploy.commands.IntakeDeployExtendCommand;
import competition.subsystems.pose.AutoLandmarks;
import competition.subsystems.pose.Landmarks;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;
import xbot.common.command.BaseSequentialCommandGroup;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Set;


public class DepotCollectionAutoCommandGroup extends BaseSequentialCommandGroup {

    @Inject
    public DepotCollectionAutoCommandGroup(Provider<SwerveSimpleTrajectoryCommand> trajectoryProvider,
                                           HopperAndIntakeCommandGroup hopperAndIntakeCommandGroup,
                                           IntakeDeployExtendCommand intakeDeployExtendCommand,
                                           HopperRollerSubsystem hopper,
                                           AutoLandmarks autoLandmarks,
                                           DriveSubsystem drive, PoseSubsystem pose,
                                           DriveToShootingPositionCommand driveToPositionCommand,
                                           AimAndShootFromHereCommand aimAndShootFromHereCommand
                                           ) {

        var readyDepotCollect = trajectoryProvider.get();

        Pose2d depotCollectCenter = PoseSubsystem.convertBlueToRedIfNeeded(Landmarks.blueDepotCollectCenter);

        ArrayList<XbotSwervePoint> readyPoint = new ArrayList<>();

        readyPoint.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                depotCollectCenter, 2));

        readyDepotCollect.logic.setKeyPoints(readyPoint);
        readyDepotCollect.logic.setConstantVelocity(drive.getMaxTargetSpeedMetersPerSecond());

        var depotCollect = trajectoryProvider.get();

        Pose2d depotTowerSidePose = PoseSubsystem.convertBlueToRedIfNeeded(Landmarks.blueDepotCollectPointShallow);

        ArrayList<XbotSwervePoint> collectPoint = new ArrayList<>();

        collectPoint.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                depotTowerSidePose, 2));

        depotCollect.logic.setKeyPoints(collectPoint);
        depotCollect.logic.setConstantVelocity(drive.getMaxTargetSpeedMetersPerSecond());

        var readyDepotCollect2 = trajectoryProvider.get();

        ArrayList<XbotSwervePoint> readyPoint2 = new ArrayList<>();

        readyPoint2.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                depotCollectCenter,1));

        readyDepotCollect2.logic.setKeyPoints(readyPoint2);
        readyDepotCollect2.logic.setConstantVelocity(drive.getMaxTargetSpeedMetersPerSecond());

        var depotCollectDeep  = trajectoryProvider.get();

        Pose2d DepotPointDeep =  PoseSubsystem.convertBlueToRedIfNeeded(Landmarks.blueDepotCollectPointDeep);

        ArrayList<XbotSwervePoint> deepCollectPoint = new ArrayList<>();

        deepCollectPoint.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                DepotPointDeep, 1)
        );

        depotCollectDeep.logic.setKeyPoints(deepCollectPoint);
        depotCollectDeep.logic.setConstantVelocity(drive.getMaxTargetSpeedMetersPerSecond());

        addCommands(
                pose.createSetPositionCommand(PoseSubsystem.convertBlueToRedIfNeeded(Landmarks.blueStartTrenchToDepot)),
                new ParallelDeadlineGroup(
                        intakeDeployExtendCommand,
                        hopperAndIntakeCommandGroup
                ),
                readyDepotCollect,
                depotCollect,
                readyDepotCollect2,
                depotCollectDeep,
                hopper.getStopCommand(),
                Commands.defer(() -> {
                            Pose2d currentPose = pose.getCurrentPose2d();
                            Pose2d endPose = autoLandmarks.getClosestShootingPose(currentPose);

                            var shootPose = trajectoryProvider.get();
                            ArrayList<XbotSwervePoint> shootPoint = new ArrayList<>();
                            shootPoint.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(endPose,
                                    1));
                            shootPose.logic.setKeyPoints(shootPoint);
                            shootPose.logic.setConstantVelocity(drive.getMaxTargetSpeedMetersPerSecond());

                            return shootPose;
                        },
                        Set.of(drive)

                ),
                aimAndShootFromHereCommand);
    }
}
