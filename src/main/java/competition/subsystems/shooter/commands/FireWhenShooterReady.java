package competition.subsystems.shooter.commands;

import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.shooter_feeder.commands.ShooterFeederFire;
import xbot.common.command.BaseSequentialCommandGroup;

public class FireWhenShooterReady extends BaseSequentialCommandGroup {
    public FireWhenShooterReady(ShooterOutputCommand shootCommand, ShooterSubsystem shooterSubsystem, ShooterFeederFire shooterFeederFireCommand) {
        this.addCommands(
                shootCommand,
                shooterSubsystem.getWaitForAtGoalCommand(),
                shooterFeederFireCommand);
    }
}


