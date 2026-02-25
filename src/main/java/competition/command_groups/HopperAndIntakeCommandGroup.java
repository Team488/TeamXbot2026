package competition.command_groups;

import competition.subsystems.fuel_intake.commands.FuelIntakeCommand;
import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import xbot.common.command.BaseParallelCommandGroup;

import javax.inject.Inject;

public class HopperAndIntakeCommandGroup extends BaseParallelCommandGroup {

    @Inject
    public HopperAndIntakeCommandGroup(HopperRollerSubsystem hopperRoller, FuelIntakeCommand fuelIntakeCommand) {
        addCommands(fuelIntakeCommand, hopperRoller.getIntakeCommand());
    }
}
