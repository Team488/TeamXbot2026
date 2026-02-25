package competition.command_groups;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.fuel_intake.commands.FuelIntakeCommand;
import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import competition.subsystems.intake_deploy.commands.IntakeDeployExtendCommand;
import competition.subsystems.intake_deploy.commands.IntakeDeployStopCommand;
import competition.subsystems.pose.Landmarks;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;
import xbot.common.command.BaseSequentialCommandGroup;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;


public class DepotCollectionAutoCommandGroup extends BaseSequentialCommandGroup {

    @Inject
    public DepotCollectionAutoCommandGroup(Provider<SwerveSimpleTrajectoryCommand> trajectoryProvider,
                                           HopperAndIntakeCommandGroup hopperAndIntakeCommandGroup,
                                           IntakeDeployExtendCommand intakeDeployExtendCommand,
                                           IntakeDeployStopCommand  intakeDeployStopCommand,
                                           HopperRollerSubsystem hopper,
                                           DriveSubsystem drive,
                                           PoseSubsystem pose) {

        var readyDepotCollect = trajectoryProvider.get();

        Pose2d depotWallSidePose = PoseSubsystem.convertBlueToRedIfNeeded(Landmarks.blueDepotWallSide);

        ArrayList<XbotSwervePoint> readyPoint = new ArrayList<>();

        readyPoint.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                depotWallSidePose, 2.5));

        readyDepotCollect.logic.setKeyPoints(readyPoint);
        readyDepotCollect.logic.setConstantVelocity(drive.getMaxTargetSpeedMetersPerSecond());

        var depotCollect = trajectoryProvider.get();

        Pose2d depotTowerSidePose = PoseSubsystem.convertBlueToRedIfNeeded(Landmarks.blueDepotTowerSide);

        ArrayList<XbotSwervePoint> collectPoint = new ArrayList<>();

        collectPoint.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                depotTowerSidePose, 3));

        depotCollect.logic.setKeyPoints(collectPoint);
        depotCollect.logic.setConstantVelocity(drive.getMaxTargetSpeedMetersPerSecond());

        addCommands(
                pose.createSetPositionCommand(PoseSubsystem.convertBlueToRedIfNeeded(Landmarks.blueStartTrenchToDepot)),
                new ParallelDeadlineGroup(
                        intakeDeployExtendCommand,
                        hopperAndIntakeCommandGroup
                ),
                readyDepotCollect,
                depotCollect,
            new ParallelCommandGroup(
                intakeDeployStopCommand,
                hopper.getStopCommand()
            )
        );
    }
}
