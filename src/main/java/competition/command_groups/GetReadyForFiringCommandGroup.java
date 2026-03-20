package competition.command_groups;

import competition.subsystems.pose.AutoLandmarks;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.pose.TrajectoriesCalculation;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import xbot.common.command.BaseSequentialCommandGroup;

import javax.inject.Inject;

public class GetReadyForFiringCommandGroup extends BaseSequentialCommandGroup {

    @Inject
    public GetReadyForFiringCommandGroup(TrajectoriesCalculation trajectoriesCalculation,
                                         FireWhenReadyShooterCommandGroup fireWhenReadyShooterCommandGroup,
                                         PoseSubsystem pose,
                                         AutoLandmarks autoLandmarks,
                                         PrepareToShootCommandGroup prepareToShootCommandGroup,
                                         DriveToShootingPositionCommand driveToShootingPositionCommand
    ) {
        prepareToShootCommandGroup.setPresetLocation(TrajectoriesCalculation.PresetShootingDistance.TRENCH);

        var getReadyToFire = new ParallelCommandGroup(
                driveToShootingPositionCommand, prepareToShootCommandGroup);

        this.addCommands(getReadyToFire);

        this.addCommands(fireWhenReadyShooterCommandGroup);
    }
}
