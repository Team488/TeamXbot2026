package competition.auto_programs;

import javax.inject.Inject;

import competition.command_groups.ContinuousPrepareToShootFromHereCommand;
import competition.command_groups.IntakeSlowlyAndFireWhenReadyWithRotation;
import competition.subsystems.drive.commands.RotateToHubCommand;
import competition.subsystems.drive.commands.SwerveDriveWithJoysticksCommand;
import xbot.common.command.BaseParallelCommandGroup;

public class AimAndShootFromHereCommand extends BaseParallelCommandGroup {

    @Inject
    public AimAndShootFromHereCommand(ContinuousPrepareToShootFromHereCommand continuousPrepareToShootFromHereCommand,
                                      IntakeSlowlyAndFireWhenReadyWithRotation fireWhenShooterAndHoodWithRotationReady,
                                      RotateToHubCommand rotateToHub, SwerveDriveWithJoysticksCommand swerve) {

        continuousPrepareToShootFromHereCommand.setTarget(ContinuousPrepareToShootFromHereCommand.ShootingTarget.HUB);
        continuousPrepareToShootFromHereCommand.setZeroHood(true);

        this.addCommands(continuousPrepareToShootFromHereCommand, rotateToHub, swerve, fireWhenShooterAndHoodWithRotationReady);
    }
}
