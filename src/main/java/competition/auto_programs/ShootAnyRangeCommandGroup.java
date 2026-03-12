package competition.auto_programs;

import javax.inject.Inject;

import competition.command_groups.ContinuousPrepareToShootCommandGroup;
import competition.command_groups.FireWhenReadyShooterCommandGroup;
import competition.subsystems.drive.commands.RotateToHubCommand;
import xbot.common.command.BaseParallelCommandGroup;

public class ShootAnyRangeCommandGroup extends BaseParallelCommandGroup {

    @Inject
    public ShootAnyRangeCommandGroup(ContinuousPrepareToShootCommandGroup continuousPrepareToShootCommandGroup,
            FireWhenReadyShooterCommandGroup fireWhenReadyShooterCommandGroup,
            RotateToHubCommand rotateToHub) {

        continuousPrepareToShootCommandGroup.setTarget(ContinuousPrepareToShootCommandGroup.ShootingTarget.HUB);

        this.addCommands(continuousPrepareToShootCommandGroup, rotateToHub, fireWhenReadyShooterCommandGroup);
    }
}
