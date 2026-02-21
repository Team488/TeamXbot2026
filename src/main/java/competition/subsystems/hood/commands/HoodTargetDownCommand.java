package competition.subsystems.hood.commands;

import competition.subsystems.hood.HoodSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class HoodTargetDownCommand extends BaseCommand {
    final HoodSubsystem hood;

    @Inject
    public HoodTargetDownCommand(HoodSubsystem hoodSubsystem) {
        this.hood = hoodSubsystem;
        addRequirements(hoodSubsystem);
    }

    @Override
    public void initialize() {
        hood.hoodTargetDown();
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
