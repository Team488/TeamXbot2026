package competition.subsystems.hood.commands;

import competition.subsystems.hood.HoodSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class HoodRetractCommand extends BaseCommand {
    final HoodSubsystem hood;

    @Inject
    public HoodRetractCommand(HoodSubsystem hoodSubsystem) {
        this.hood = hoodSubsystem;
        addRequirements(hoodSubsystem);
    }

    @Override
    public void initialize() {
        hood.retract();
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
