package competition.subsystems.shooter.commands;

import competition.subsystems.shooter.ShooterSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class TrimShooterVelocityUp extends BaseCommand {
    ShooterSubsystem shooter;

    @Inject
    public TrimShooterVelocityUp(ShooterSubsystem shooterSubsystem) {
        shooter = shooterSubsystem;
        this.addRequirements(shooter);
    }

    @Override
    public void initialize() {
        shooter.increaseTargetVelocity();
    }
}
