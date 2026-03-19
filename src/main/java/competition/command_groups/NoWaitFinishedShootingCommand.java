package competition.command_groups;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.RPM;

import competition.Robot;
import competition.subsystems.collector_intake.commands.CollectorIntakeCommand;
import competition.subsystems.collector_intake.commands.CollectorStopCommand;
import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.hood.commands.HoodSetCommand;
import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.shooter.commands.ShooterOutputCommand;
import competition.subsystems.shooter.commands.ShooterStopCommand;
import competition.subsystems.shooter_feeder.commands.ShooterFeederFire;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import xbot.common.command.BaseParallelCommandGroup;
import xbot.common.command.NamedInstantCommand;

public class NoWaitFinishedShootingCommand extends BaseParallelCommandGroup {

    @Inject
    public NoWaitFinishedShootingCommand(HopperRollerSubsystem hopper,
             ShooterStopCommand shooterStopCommand,
             HoodSetCommand setHoodCommand,
             CollectorStopCommand collectorStopCommand) {
        setHoodCommand.setTargetRatio(0.0);

        var group = new ParallelCommandGroup(setHoodCommand, shooterStopCommand, hopper.getStopCommand(), collectorStopCommand);

        // Stop after one cycle as some of these commands won't end
        addCommands(group.withTimeout(Robot.LOOP_INTERVAL));
    }
}
