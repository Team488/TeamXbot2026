package competition.subsystems.shooter.commands;

import competition.subsystems.shooter.ShooterSubsystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class TrimShooterVelocityUp extends BaseCommand {
    ShooterSubsystem shooter;
    Logger log = LogManager.getLogger(TrimShooterVelocityUp.class);

    @Inject
    public TrimShooterVelocityUp(ShooterSubsystem shooterSubsystem) {
        shooter = shooterSubsystem;
        this.addRequirements(shooter);
    }

    @Override
    public void initialize() {
        shooter.increaseTargetVelocity();
        log.info("Decreasing target velocity to " + shooter.targetVelocity.get());
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
