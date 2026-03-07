package competition.command_groups;

import competition.subsystems.collector_intake.commands.CollectorIntakeCommand;
import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.shooter.commands.ShooterOutputCommand;
import competition.subsystems.shooter_feeder.commands.ShooterFeederFire;
import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import xbot.common.command.BaseParallelCommandGroup;
import xbot.common.command.NamedInstantCommand;

import javax.inject.Inject;

public class FireWhenShooterReadyCommandGroup extends BaseParallelCommandGroup {

    @Inject
    public FireWhenShooterReadyCommandGroup(HopperRollerSubsystem hopper, ShooterSubsystem shooterSubsystem,
                                            HoodSubsystem hoodSubsystem,
                                            ShooterOutputCommand shooterOutputCommand,
                                            ShooterFeederFire shooterFeederFireCommand,
                                            CollectorIntakeCommand fuelIntakeCommand
    ) {
        var waitForShooterCommand = shooterSubsystem.getWaitForAtGoalCommand();
        var waitForHoodCommand =  hoodSubsystem.getWaitForAtGoalCommand();
        var hopperIntakeCommand = hopper.getIntakeCommand();

        var setHoodCommand = new NamedInstantCommand("Set Hood Min", () -> hoodSubsystem.setTargetValue(0.0));
        var waitForShooterAndHood = waitForShooterCommand.alongWith(waitForHoodCommand);
        var startFeedingSequence = shooterFeederFireCommand
                .alongWith(new WaitCommand(0.5)
                        .andThen(fuelIntakeCommand.alongWith(hopperIntakeCommand)));

        this.addCommands(
                setHoodCommand,
                shooterOutputCommand,
                waitForShooterAndHood.andThen(startFeedingSequence)
        );
    }
}