package competition.subsystems.hopper_roller.commands;

import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class HopperRollerStopCommand extends BaseCommand {
    final HopperRollerSubsystem hopperRollerSubsystem;

    @Inject
    public HopperRollerStopCommand(HopperRollerSubsystem hopperRollerSubsystem) {
        this.hopperRollerSubsystem = hopperRollerSubsystem;
        this.addRequirements(this.hopperRollerSubsystem);
    }

    @Override
    public void initialize() {
        hopperRollerSubsystem.getStopCommand();
    }
}
