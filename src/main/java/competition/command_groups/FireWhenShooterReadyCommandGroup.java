package competition.command_groups;

import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.shooter.commands.ShooterOutputCommand;
import competition.subsystems.shooter_feeder.commands.ShooterFeederFire;
import xbot.common.command.BaseSequentialCommandGroup;

public class FireWhenShooterReadyCommandGroup extends BaseSequentialCommandGroup {
    public FireWhenShooterReadyCommandGroup(ShooterOutputCommand shootCommand, ShooterSubsystem shooterSubsystem, ShooterFeederFire shooterFeederFireCommand) {
        this.addCommands(
                shootCommand,
                shooterSubsystem.getWaitForAtGoalCommand(),
                shooterFeederFireCommand);
    }
}


