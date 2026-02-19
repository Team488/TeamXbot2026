package competition.subsystems.hood.commands;

import competition.subsystems.hood.HoodSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class HoodExtendCommand extends BaseCommand {
    final HoodSubsystem hood;

    @Inject
    public HoodExtendCommand(HoodSubsystem hoodSubsystem) {
        this.hood = hoodSubsystem;
        addRequirements(hoodSubsystem);
    }

    @Override
    public void initialize() {
        hood.extend();
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
