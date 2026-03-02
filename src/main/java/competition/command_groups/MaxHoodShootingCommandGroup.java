package competition.command_groups;

import competition.subsystems.fuel_intake.commands.FuelIntakeCommand;
import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.shooter.commands.ShooterOutputCommand;
import competition.subsystems.shooter_feeder.commands.ShooterFeederFire;
import edu.wpi.first.wpilibj2.command.WaitCommand;
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

        var setHoodCommand = new NamedInstantCommand("Set Hood Min", () -> hoodSubsystem.setTargetValue(1.0));
        var waitForShooterAndHood = waitForShooterCommand.alongWith(waitForHoodCommand);
        var startFeedingSequence = shooterFeederFire
                .alongWith(new WaitCommand(0.5)
                        .andThen(fuelIntakeCommand.alongWith(hopperIntakeCommand)));

        this.addCommands(
                setHoodCommand,
                shooterOutputCommand,
                waitForShooterAndHood.andThen(startFeedingSequence)
        );
    }
}