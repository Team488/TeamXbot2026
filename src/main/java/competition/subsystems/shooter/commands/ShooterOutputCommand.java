package competition.subsystems.shooter.commands;

import competition.subsystems.shooter.ShooterSubsystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class ShooterOutputCommand extends BaseCommand {
    ShooterSubsystem shooter;
    Logger log = LogManager.getLogger(ShooterOutputCommand.class);

    @Inject
    public ShooterOutputCommand(ShooterSubsystem shooterSubsystem) {
        shooter = shooterSubsystem;
        this.addRequirements(shooter);
    }

    @Override
    public void initialize() {
        shooter.runAtTargetVelocity();
        log.info("Shooting at " + shooter.targetVelocity.get() + " RPM");
    }
}
