package competition.subsystems.hood.commands;

import competition.subsystems.hood.HoodSubsystem;

import competition.subsystems.shooter.commands.TrimShooterVelocityUp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class HoodToZeroCommand extends BaseCommand {
    final HoodSubsystem hood;

    @Inject
    public HoodToZeroCommand(HoodSubsystem hoodSubsystem) {
        this.hood = hoodSubsystem;
        addRequirements(hoodSubsystem);
    }

    @Override
    public void initialize() {
        hood.servoZero();
        log.info("running servo to " + hood.servoMin.get());
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
