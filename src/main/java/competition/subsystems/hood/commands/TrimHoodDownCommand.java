package competition.subsystems.hood.commands;

import competition.subsystems.hood.HoodSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class TrimHoodDownCommand extends BaseCommand {
    final HoodSubsystem hood;

    @Inject
    public TrimHoodDownCommand(HoodSubsystem hoodSubsystem) {
        this.hood = hoodSubsystem;
        addRequirements(hoodSubsystem);
    }

    @Override
    public void initialize() {
        hood.trimHoodGoalDown();
        log.info("Decreasing hood trim to " + hood.trimValue.get());
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
