package competition.subsystems.hood.commands;

import competition.subsystems.hood.HoodSubsystem;
import xbot.common.command.BaseSetpointCommand;

import javax.inject.Inject;

public class HoodExtendCommands extends BaseSetpointCommand {
    final HoodSubsystem hood;

    @Inject
    public HoodExtendCommands(HoodSubsystem hoodSubsystem) {
        super(hoodSubsystem);
        hood = hoodSubsystem;
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
