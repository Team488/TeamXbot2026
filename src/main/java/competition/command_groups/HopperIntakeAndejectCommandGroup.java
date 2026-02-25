package competition.command_groups;

import competition.subsystems.fuel_intake.commands.FuelEjectCommand;
import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import xbot.common.command.BaseParallelCommandGroup;

public class HopperIntakeAndejectCommandGroup extends BaseParallelCommandGroup {

    public HopperIntakeAndejectCommandGroup(FuelEjectCommand eject, HopperRollerSubsystem hopper) {
        addCommands(eject, hopper.getIntakeCommand());
    }
}