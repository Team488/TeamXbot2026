package competition.subsystems.shooter.commands;

import competition.subsystems.shooter.ShooterSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class TrimShooterVelocityDown extends BaseCommand {
    ShooterSubsystem shooter;

    @Inject
    public TrimShooterVelocityDown(ShooterSubsystem shooterSubsystem) {
        shooter = shooterSubsystem;
        this.addRequirements(shooter);
    }

    @Override
    public void initialize() {
        shooter.decreaseTargetVelocity();
        aKitLog.record("Current target velocity", shooter.targetVelocity.get());
    }
}
