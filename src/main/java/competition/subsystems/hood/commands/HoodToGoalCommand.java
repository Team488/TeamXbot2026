package competition.subsystems.hood.commands;

import competition.subsystems.hood.HoodSubsystem;

import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class HoodToGoalCommand extends BaseCommand {
    final HoodSubsystem hood;

    @Inject
    public HoodToGoalCommand(HoodSubsystem hoodSubsystem) {
        this.hood = hoodSubsystem;
        addRequirements(hoodSubsystem);
    }

    @Override
    public void initialize() {
        hood.runServo();
    }
}
