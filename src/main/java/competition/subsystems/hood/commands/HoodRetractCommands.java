package competition.subsystems.hood.commands;

import competition.subsystems.hood.HoodSubsystem;
import xbot.common.command.BaseSetpointCommand;

import javax.inject.Inject;

public class HoodRetractCommands extends BaseSetpointCommand {
    final HoodSubsystem hood;

    @Inject
    public HoodRetractCommands(HoodSubsystem hoodSubsystem) {
        super(hoodSubsystem);
        hood = hoodSubsystem;
    }

    @Override
    public void initialize() {
        super.initialize();
        hood.retract();
        log.info("Initialized HoodRetract");
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
