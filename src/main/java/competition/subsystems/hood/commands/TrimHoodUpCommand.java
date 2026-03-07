package competition.subsystems.hood.commands;

import com.fasterxml.jackson.databind.ser.Serializers;
import competition.subsystems.hood.HoodSubsystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;


public class TrimHoodUpCommand extends BaseCommand {
    final HoodSubsystem hood;

    @Inject
    public TrimHoodUpCommand(HoodSubsystem hoodSubsystem) {
        this.hood = hoodSubsystem;
        addRequirements(hoodSubsystem);
    }

    @Override
    public void initialize() {
        hood.trimHoodGoalUp();
        log.info("Increasing hood trim to " + hood.trimValue.get());
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
