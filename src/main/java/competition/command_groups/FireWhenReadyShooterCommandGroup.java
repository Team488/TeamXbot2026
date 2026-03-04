package competition.command_groups;

import competition.subsystems.fuel_intake.commands.FuelIntakeCommand;
import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.shooter.commands.ShooterOutputCommand;
import competition.subsystems.shooter_feeder.commands.ShooterFeederFire;
import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import xbot.common.command.BaseParallelCommandGroup;

import javax.inject.Inject;

public class FireWhenReadyShooterCommandGroup extends BaseParallelCommandGroup {

    @Inject
    public FireWhenReadyShooterCommandGroup(HopperRollerSubsystem hopper, ShooterSubsystem shooterSubsystem,
                                            HoodSubsystem hoodSubsystem,
                                            ShooterOutputCommand shooterOutputCommand,
                                            ShooterFeederFire shooterFeederFireCommand,
                                            FuelIntakeCommand fuelIntakeCommand
    ) {
        var waitForShooterCommand = shooterSubsystem.getWaitForAtGoalCommand();
        var waitForHoodCommand =  hoodSubsystem.getWaitForAtGoalCommand();
        var hopperIntakeCommand = hopper.getIntakeCommand();
        this.addCommands(
                shooterOutputCommand,
                waitForShooterCommand.alongWith(waitForHoodCommand)
                        .andThen(hopperIntakeCommand.alongWith(shooterFeederFireCommand, fuelIntakeCommand))
        );
    }
}