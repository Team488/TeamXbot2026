package competition.command_groups;

import javax.inject.Inject;

import competition.subsystems.collector_intake.commands.CollectorStopCommand;
import competition.subsystems.hood.commands.HoodSetCommand;
import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import competition.subsystems.shooter.commands.ShooterStopCommand;
import competition.subsystems.shooter_feeder.commands.ShooterFeederStop;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;
import xbot.common.command.BaseParallelCommandGroup;

public class NoWaitFinishedShootingCommand extends BaseParallelCommandGroup {

    @Inject
    public NoWaitFinishedShootingCommand(HopperRollerSubsystem hopper,
             ShooterStopCommand shooterStopCommand,
             ShooterFeederStop shooterFeederStop,
             HoodSetCommand setHoodCommand,
             CollectorStopCommand collectorStopCommand) {
        setHoodCommand.setTargetRatio(0.0);

        // Use InstantCommand as a deadline to make sure the other commands are only scheduled for one cycle,
        // but run in parallel so they can stop the subsystems immediately.
        var group = new ParallelDeadlineGroup(
                new InstantCommand(),
                setHoodCommand,
                shooterStopCommand,
                shooterFeederStop,
                hopper.getStopCommand(),
                collectorStopCommand);

        addCommands(group);
    }
}
