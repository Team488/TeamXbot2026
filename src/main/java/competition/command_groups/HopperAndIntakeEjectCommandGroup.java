package competition.command_groups;

import competition.subsystems.fuel_intake.commands.FuelEjectCommand;
import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import xbot.common.command.BaseParallelCommandGroup;

import javax.inject.Inject;

public class HopperAndIntakeEjectCommandGroup extends BaseParallelCommandGroup {

    @Inject
    public HopperAndIntakeEjectCommandGroup(HopperRollerSubsystem hopperRoller, FuelEjectCommand fuelEjectCommand){
        addCommands(fuelEjectCommand, hopperRoller.getEjectCommand());
    }
}
