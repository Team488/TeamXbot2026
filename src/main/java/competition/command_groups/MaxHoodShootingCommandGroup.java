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

import static edu.wpi.first.units.Units.RPM;

public class MaxHoodShootingCommandGroup extends BaseParallelCommandGroup {

    @Inject
    public MaxHoodShootingCommandGroup(HopperRollerSubsystem hopperRollerSubsystem,
                                       HoodSubsystem hoodSubsystem,
                                       ShooterFeederFire shooterFeederFireCommand,
                                       FuelIntakeCommand fuelIntakeCommand,
                                       ShooterSubsystem shooterSubsystem,
                                       PrepareToShootCommandGroup prepareToShootCommandGroup
    ) {
        var waitForHoodCommand =  hoodSubsystem.getWaitForAtGoalCommand();
        var waitForShooterCommand = shooterSubsystem.getWaitForAtGoalCommand();
        var hopperIntakeCommand = hopperRollerSubsystem.getIntakeCommand();

        var startFeedingSequence = waitForShooterCommand.alongWith(waitForHoodCommand).andThen(shooterFeederFireCommand)
                .alongWith(new WaitCommand(0.5)
                        .andThen(fuelIntakeCommand.alongWith(hopperIntakeCommand)));

        prepareToShootCommandGroup.setShooterGoal(RPM.of(4800));
        prepareToShootCommandGroup.setHoodGoal(1.0);

        this.addCommands(
                prepareToShootCommandGroup
                .andThen(startFeedingSequence)
        );
    }
}