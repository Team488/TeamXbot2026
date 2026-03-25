package competition.subsystems.hood.commands;

import competition.subsystems.hood.HoodSubsystem;
import xbot.common.command.BaseSetpointCommand;

import javax.inject.Inject;

public class HoodToZeroCommand extends BaseSetpointCommand {
    HoodSubsystem hoodSubsystem;

    @Inject
    public HoodToZeroCommand(HoodSubsystem hoodSubsystem) {
        super(hoodSubsystem);
        this.hoodSubsystem = hoodSubsystem;
    }

    @Override
    public void initialize() {
        super.initialize();
        this.hoodSubsystem.setTargetValue(0.0);
    }
}
