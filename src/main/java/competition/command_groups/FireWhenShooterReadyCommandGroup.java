package competition.command_groups;

import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.shooter.commands.ShooterOutputCommand;
import competition.subsystems.shooter_feeder.commands.ShooterFeederFire;
import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import xbot.common.command.BaseParallelCommandGroup;

import javax.inject.Inject;


public class FireWhenShooterReadyCommandGroup extends BaseParallelCommandGroup {

    @Inject
    public FireWhenShooterReadyCommandGroup(HopperRollerSubsystem hopper, ShooterSubsystem shooterSubsystem,
                                            ShooterOutputCommand shooterOutputCommand,
                                            ShooterFeederFire shooterFeederFireCommand) {

        var waitForShooterCommand = shooterSubsystem.getWaitForAtGoalCommand();
        var hopperIntakeCommand = hopper.getIntakeCommand();
        this.addCommands(
                shooterOutputCommand,
                waitForShooterCommand.andThen(hopperIntakeCommand.alongWith(shooterFeederFireCommand))
        );
    }
}

