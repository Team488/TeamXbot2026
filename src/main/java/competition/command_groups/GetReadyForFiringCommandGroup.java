package competition.command_groups;

import competition.subsystems.pose.AutoLandmarks;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.pose.TrajectoriesCalculation;
import xbot.common.command.BaseParallelCommandGroup;
import static edu.wpi.first.units.Units.RPM;

import javax.inject.Inject;

public class GetReadyForFiringCommandGroup extends BaseParallelCommandGroup {

    @Inject
    public GetReadyForFiringCommandGroup(TrajectoriesCalculation trajectoriesCalculation,
                                         FireWhenReadyShooterCommandGroup fireWhenReadyShooterCommandGroup,
                                         PoseSubsystem pose,
                                         AutoLandmarks autoLandmarks,
                                         PrepareToShootCommandGroup prepareToShootCommandGroup,
                                         DriveToShootingPositionCommand driveToShootingPositionCommand
    ) {

        var currentPose = pose.getCurrentPose2d();
        var startPose = autoLandmarks.getAllianceShootingStartingPose(currentPose);
        var endPose = autoLandmarks.getClosestShootingPose(startPose);
        var shootingData = trajectoriesCalculation.calculateAllianceHubShootingData(endPose);

        prepareToShootCommandGroup.setShooterGoal(RPM.of(4800));
        prepareToShootCommandGroup.setHoodGoal(shootingData.servoRatio());

        this.addCommands(
                driveToShootingPositionCommand,
                prepareToShootCommandGroup
                        .andThen(fireWhenReadyShooterCommandGroup)

        );
    }
}