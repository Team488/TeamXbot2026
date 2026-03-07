package competition.command_groups;

import competition.subsystems.fuel_intake.commands.FuelIntakeCommand;
import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import competition.subsystems.shooter_feeder.commands.ShooterFeederFire;
import xbot.common.command.BaseParallelCommandGroup;

import javax.inject.Inject;

public class RunCollectorHopperFeederCommandGroup extends BaseParallelCommandGroup {

    @Inject
    public RunCollectorHopperFeederCommandGroup(HopperRollerSubsystem hopper,
                                            FuelIntakeCommand fuelIntakeCommand,
                                            ShooterFeederFire shooterFeederFireCommand
    ) {
        var hopperIntakeCommand = hopper.getIntakeCommand();
        this.addCommands(
                hopperIntakeCommand.alongWith(fuelIntakeCommand).alongWith(shooterFeederFireCommand)
        );
    }
}