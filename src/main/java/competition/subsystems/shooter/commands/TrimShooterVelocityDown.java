package competition.subsystems.shooter.commands;

import competition.subsystems.shooter.ShooterSubsystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class TrimShooterVelocityDown extends BaseCommand {
    ShooterSubsystem shooter;
    Logger log = LogManager.getLogger(TrimShooterVelocityDown.class);

    @Inject
    public TrimShooterVelocityDown(ShooterSubsystem shooterSubsystem) {
        shooter = shooterSubsystem;
        this.addRequirements(shooter);
    }

    @Override
    public void initialize() {
        shooter.decreaseTargetVelocity();
        log.info("Decreasing target velocity to " + shooter.targetVelocity.get());
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
