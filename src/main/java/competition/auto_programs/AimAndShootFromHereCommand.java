package competition.auto_programs;

import javax.inject.Inject;

import competition.command_groups.ContinuousPrepareToShootFromHereCommand;
import competition.command_groups.FireWhenShooterReady;
import competition.subsystems.drive.commands.RotateToHubCommand;
import xbot.common.command.BaseParallelCommandGroup;

public class AimAndShootFromHereCommand extends BaseParallelCommandGroup {

    @Inject
    public AimAndShootFromHereCommand(ContinuousPrepareToShootFromHereCommand continuousPrepareToShootFromHereCommand,
            FireWhenShooterReady fireWhenShooterReady,
            RotateToHubCommand rotateToHub) {

        continuousPrepareToShootFromHereCommand.setTarget(ContinuousPrepareToShootFromHereCommand.ShootingTarget.HUB);

        this.addCommands(continuousPrepareToShootFromHereCommand, rotateToHub, fireWhenShooterReady);
    }
}
