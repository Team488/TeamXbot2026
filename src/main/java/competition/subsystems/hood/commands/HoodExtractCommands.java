package competition.subsystems.hood.commands;

import competition.subsystems.hood.HoodSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class HoodExtractCommands extends BaseCommand {
    final HoodSubsystem hood;

    @Inject
    public HoodExtractCommands(HoodSubsystem hoodSubsystem) {
        hood = hoodSubsystem;
        this.addRequirements(hood);
    }

    @Override
    public void initialize() {
        hood.extend();
        log.info("Initialized HoodExtract");
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
