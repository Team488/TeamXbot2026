package competition.subsystems.shooter.commands;

import competition.subsystems.shooter.ShooterSubsystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xbot.common.command.BaseCommand;
import xbot.common.command.BaseSetpointCommand;

import javax.inject.Inject;

public class TrimShooterVelocityDown extends BaseCommand {
    final ShooterSubsystem shooter;

    @Inject
    public TrimShooterVelocityDown(ShooterSubsystem shooterSubsystem) {
        shooter = shooterSubsystem;
        this.addRequirements(shooter.getTrimSetpointLock());
    }

    @Override
    public void initialize() {
        shooter.decreaseShooterOffset();
        log.info("Decreasing shooter trim to " + shooter.trimValue.get());
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
