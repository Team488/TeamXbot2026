package competition.subsystems.hood.commands;

import competition.subsystems.hood.HoodSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class HoodRetractCommands extends BaseCommand {
    final HoodSubsystem hood;

    @Inject
    public HoodRetractCommands(HoodSubsystem hoodSubsystem) {
        hood = hoodSubsystem;
        this.addRequirements(hood);
    }

    @Override
    public void initialize() {
        hood.retract();
        log.info("Initialized HoodRetract");
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
