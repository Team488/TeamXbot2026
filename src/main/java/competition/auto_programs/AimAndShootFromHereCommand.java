package competition.auto_programs;

import javax.inject.Inject;

import competition.command_groups.ContinuousPrepareToShootFromHereCommand;
import competition.command_groups.FireWhenShooterAndHoodReady;
import competition.subsystems.drive.commands.RotateToHubCommand;
import xbot.common.command.BaseParallelCommandGroup;

public class AimAndShootFromHereCommand extends BaseParallelCommandGroup {

    @Inject
    public AimAndShootFromHereCommand(ContinuousPrepareToShootFromHereCommand continuousPrepareToShootFromHereCommand,
            FireWhenShooterAndHoodReady fireWhenShooterAndHoodReady,
            RotateToHubCommand rotateToHub) {

        continuousPrepareToShootFromHereCommand.setTarget(ContinuousPrepareToShootFromHereCommand.ShootingTarget.HUB);
        continuousPrepareToShootFromHereCommand.setZeroHood(true);

        this.addCommands(continuousPrepareToShootFromHereCommand, rotateToHub, fireWhenShooterAndHoodReady);
    }
}
