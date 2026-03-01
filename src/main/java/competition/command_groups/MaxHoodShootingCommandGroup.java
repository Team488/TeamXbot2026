package competition.command_groups;

import competition.subsystems.fuel_intake.commands.FuelIntakeCommand;
import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.shooter.commands.ShooterOutputCommand;
import competition.subsystems.shooter_feeder.commands.ShooterFeederFire;
import xbot.common.command.BaseParallelCommandGroup;
import xbot.common.command.NamedInstantCommand;

import javax.inject.Inject;

public class MaxHoodShootingCommandGroup extends BaseParallelCommandGroup {

    @Inject
    public MaxHoodShootingCommandGroup(HopperRollerSubsystem hopperRollerSubsystem,
                                       HoodSubsystem hoodSubsystem,
                                       ShooterFeederFire shooterFeederFire,
                                       FuelIntakeCommand fuelIntakeCommand,
                                       ShooterSubsystem shooterSubsystem,
                                       ShooterOutputCommand shooterOutputCommand) {
        var waitForHoodCommand =  hoodSubsystem.getWaitForAtGoalCommand();
        var waitForShooterCommand = shooterSubsystem.getWaitForAtGoalCommand();
        var hopperIntakeCommand = hopperRollerSubsystem.getIntakeCommand();
        this.addCommands(
                new NamedInstantCommand("Set Hood Max", () -> hoodSubsystem.setTargetValue(1.0))
                        .andThen(shooterOutputCommand).alongWith(waitForHoodCommand).alongWith(waitForShooterCommand)
                        .andThen(hopperIntakeCommand.alongWith(shooterFeederFire, fuelIntakeCommand))
        );
    }
}