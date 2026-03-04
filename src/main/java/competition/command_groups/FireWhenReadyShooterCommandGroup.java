package competition.command_groups;

import competition.subsystems.fuel_intake.commands.FuelIntakeCommand;
import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import xbot.common.command.BaseParallelCommandGroup;

import javax.inject.Inject;

public class FireWhenReadyShooterCommandGroup extends BaseParallelCommandGroup {

    @Inject
    public FireWhenReadyShooterCommandGroup(HopperRollerSubsystem hopper, ShooterSubsystem shooterSubsystem,
                                            FuelIntakeCommand fuelIntakeCommand
    ) {
        var waitForShooterCommand = shooterSubsystem.getWaitForAtGoalCommand();
        var hopperIntakeCommand = hopper.getIntakeCommand();
        this.addCommands(
                waitForShooterCommand
                        .andThen(hopperIntakeCommand.alongWith(fuelIntakeCommand))
        );
    }
}