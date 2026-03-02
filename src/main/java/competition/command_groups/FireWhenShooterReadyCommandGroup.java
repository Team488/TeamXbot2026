package competition.command_groups;

import competition.subsystems.fuel_intake.commands.FuelIntakeCommand;
import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.shooter.commands.ShooterOutputCommand;
import competition.subsystems.shooter_feeder.commands.ShooterFeederFire;
import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import xbot.common.command.BaseParallelCommandGroup;
import xbot.common.command.NamedInstantCommand;

import javax.inject.Inject;

public class FireWhenShooterReadyCommandGroup extends BaseParallelCommandGroup {

    @Inject
    public FireWhenShooterReadyCommandGroup(HopperRollerSubsystem hopper, ShooterSubsystem shooterSubsystem,
                                            HoodSubsystem hoodSubsystem,
                                            ShooterOutputCommand shooterOutputCommand,
                                            ShooterFeederFire shooterFeederFireCommand,
                                            FuelIntakeCommand fuelIntakeCommand
    ) {
        var waitForShooterCommand = shooterSubsystem.getWaitForAtGoalCommand();
        var waitForHoodCommand =  hoodSubsystem.getWaitForAtGoalCommand();
        var hopperIntakeCommand = hopper.getIntakeCommand();
        this.addCommands(

                new NamedInstantCommand("Set Hood Min", () -> hoodSubsystem.setTargetValue(0.0))
                        .andThen(shooterOutputCommand).alongWith(waitForShooterCommand, waitForHoodCommand)
                        .andThen(shooterFeederFireCommand).alongWith(fuelIntakeCommand)
                        .andThen(hopperIntakeCommand));
    }
}