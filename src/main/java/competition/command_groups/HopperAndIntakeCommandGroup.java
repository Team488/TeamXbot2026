package competition.command_groups;

import competition.subsystems.fuel_intake.commands.FuelIntakeCommand;
import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import xbot.common.command.BaseParallelCommandGroup;

public class HopperAndIntakeCommandGroup extends BaseParallelCommandGroup {

    public HopperAndIntakeCommandGroup(FuelIntakeCommand intake, HopperRollerSubsystem hopper) {
        addCommands(
                intake,
                hopper.getEjectCommand());
    }
}
