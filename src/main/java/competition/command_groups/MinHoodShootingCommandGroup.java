package competition.command_groups;

import competition.subsystems.fuel_intake.commands.FuelIntakeCommand;
import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.shooter.commands.ShooterOutputCommand;
import competition.subsystems.shooter_feeder.commands.ShooterFeederFire;
import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import edu.wpi.first.units.Units;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import xbot.common.command.BaseParallelCommandGroup;
import xbot.common.command.NamedInstantCommand;
import competition.command_groups.PrepareToShootCommandGroup;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.RPM;

public class MinHoodShootingCommandGroup extends BaseParallelCommandGroup {

    @Inject
    public MinHoodShootingCommandGroup(HopperRollerSubsystem hopper, ShooterSubsystem shooterSubsystem,
                                       HoodSubsystem hoodSubsystem,
                                       ShooterFeederFire shooterFeederFireCommand,
                                       FuelIntakeCommand fuelIntakeCommand,
                                       PrepareToShootCommandGroup prepareToShootCommandGroup
    ) {
        var waitForShooterCommand = shooterSubsystem.getWaitForAtGoalCommand();
        var waitForHoodCommand =  hoodSubsystem.getWaitForAtGoalCommand();
        var hopperIntakeCommand = hopper.getIntakeCommand();

        var startFeedingSequence = waitForShooterCommand.alongWith(waitForHoodCommand).andThen(shooterFeederFireCommand)
                .alongWith(new WaitCommand(0.5)
                        .andThen(fuelIntakeCommand.alongWith(hopperIntakeCommand)));

        prepareToShootCommandGroup.setShooterGoal(RPM.of(4800));
        prepareToShootCommandGroup.setHoodGoal(0.0);

        this.addCommands(
                prepareToShootCommandGroup
                .andThen(startFeedingSequence)
        );
    }
}