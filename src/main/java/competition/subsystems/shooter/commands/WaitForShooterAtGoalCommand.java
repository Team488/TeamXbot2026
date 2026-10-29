package competition.subsystems.shooter.commands;

import javax.inject.Inject;

import competition.subsystems.shooter.ShooterSubsystem;
import xbot.common.command.BaseCommand;

public class WaitForShooterAtGoalCommand extends BaseCommand {

    final ShooterSubsystem shooterSubsystem;

    @Inject
    public WaitForShooterAtGoalCommand(ShooterSubsystem shooterSubsystem) {
        this.shooterSubsystem = shooterSubsystem;
    }

    @Override
    public boolean isFinished() {
        return shooterSubsystem.isReadyToFire();
    }
    
}
