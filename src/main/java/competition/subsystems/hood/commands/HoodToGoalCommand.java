package competition.subsystems.hood.commands;

import competition.subsystems.hood.HoodSubsystem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class HoodToGoalCommand extends BaseCommand {
    final HoodSubsystem hood;
    final Logger log = LogManager.getLogger(HoodToGoalCommand.class);

    @Inject
    public HoodToGoalCommand(HoodSubsystem hoodSubsystem) {
        this.hood = hoodSubsystem;
        addRequirements(hoodSubsystem);
    }

    @Override
    public void initialize() {
        hood.runServo();
        log.info("running servo to " + (((hood.servoMax.get() - hood.servoMin.get()) * hood.servoDistancePercent.get()) + hood.servoMin.get()));
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
