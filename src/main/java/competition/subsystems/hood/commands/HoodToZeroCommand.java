package competition.subsystems.hood.commands;

import competition.subsystems.hood.HoodSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class HoodToZeroCommand extends BaseCommand {
    final HoodSubsystem hood;

    @Inject
    public HoodToZeroCommand(HoodSubsystem hoodSubsystem) {
        this.hood = hoodSubsystem;
        addRequirements(hoodSubsystem);
    }

    @Override
    public void initialize() {
        hood.stopServo();
    }
}
