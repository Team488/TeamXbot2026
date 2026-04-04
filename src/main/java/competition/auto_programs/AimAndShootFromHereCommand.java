package competition.auto_programs;

import javax.inject.Inject;

import competition.command_groups.ContinuousPrepareToShootFromHereCommand;
import competition.command_groups.IntakeSlowlyAndFireWhenReady;
import competition.subsystems.drive.commands.RotateToHubCommand;
import competition.subsystems.drive.commands.SwerveDriveWithJoysticksCommand;
import xbot.common.command.BaseParallelCommandGroup;

public class AimAndShootFromHereCommand extends BaseParallelCommandGroup {

    @Inject
    public AimAndShootFromHereCommand(ContinuousPrepareToShootFromHereCommand continuousPrepareToShootFromHereCommand,
                                      IntakeSlowlyAndFireWhenReady fireWhenShooterAndHoodReady,
                                      RotateToHubCommand rotateToHub, SwerveDriveWithJoysticksCommand swerve) {

        continuousPrepareToShootFromHereCommand.setTarget(ContinuousPrepareToShootFromHereCommand.ShootingTarget.HUB);
        continuousPrepareToShootFromHereCommand.setZeroHood(true);

        this.addCommands(continuousPrepareToShootFromHereCommand, rotateToHub, swerve, fireWhenShooterAndHoodReady);
    }
}
