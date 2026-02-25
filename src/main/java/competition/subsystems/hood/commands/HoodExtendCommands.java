package competition.subsystems.hood.commands;

import competition.subsystems.hood.HoodSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class HoodExtendCommands extends BaseCommand {
    final HoodSubsystem hood;

    @Inject
    public HoodExtendCommands(HoodSubsystem hoodSubsystem) {
        hood = hoodSubsystem;
        this.addRequirements(hood);
    }

    @Override
    public void initialize() {
        hood.extend();
        log.info("Initialized HoodExtend");
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
