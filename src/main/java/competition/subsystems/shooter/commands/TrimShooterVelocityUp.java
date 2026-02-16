package competition.subsystems.shooter.commands;

import competition.subsystems.shooter.ShooterSubsystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class TrimShooterVelocityUp extends BaseCommand {
    final ShooterSubsystem shooter;

    @Inject
    public TrimShooterVelocityUp(ShooterSubsystem shooterSubsystem) {
        shooter = shooterSubsystem;
        this.addRequirements(shooter);
    }

    @Override
    public void initialize() {
        shooter.increaseShooterOffset();
        log.info("Increasing hood trim to " + shooter.trimValue.get());
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
