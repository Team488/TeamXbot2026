package competition.subsystems.hopper_roller.commands;

import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class HopperEjectCommand extends BaseCommand {
    final HopperRollerSubsystem hopperRollerSubsystem;

    @Inject
    public HopperEjectCommand(HopperRollerSubsystem hopperRollerSubsystem) {
        this.hopperRollerSubsystem = hopperRollerSubsystem;
        this.addRequirements(this.hopperRollerSubsystem);
    }

    @Override
    public void initialize() {
        hopperRollerSubsystem.setEjectPower();
        log.info("Initialized HopperEject");
    }

}