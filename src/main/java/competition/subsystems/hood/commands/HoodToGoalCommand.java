package competition.subsystems.hood.commands;

import competition.subsystems.hood.HoodSubsystem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class HoodToGoalCommand extends BaseCommand {
    final HoodSubsystem hood;

    @Inject
    public HoodToGoalCommand(HoodSubsystem hoodSubsystem) {
        this.hood = hoodSubsystem;
        addRequirements(hoodSubsystem);
    }

    @Override
    public void initialize() {
        hood.runServo();
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
