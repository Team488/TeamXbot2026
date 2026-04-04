package competition.command_groups;

import competition.subsystems.collector_intake.commands.CollectorIntakeCommand;
import competition.subsystems.collector_intake.commands.SlowCollectorIntakeCommand;
import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import competition.subsystems.shooter_feeder.commands.ShooterFeederFire;
import xbot.common.command.BaseParallelCommandGroup;

import javax.inject.Inject;

public class RunCollectorHopperFeederCommandGroup extends BaseParallelCommandGroup {

    @Inject
    public RunCollectorHopperFeederCommandGroup(HopperRollerSubsystem hopper,
                                            SlowCollectorIntakeCommand slowCollectorIntakeCommand,
                                            ShooterFeederFire shooterFeederFireCommand
    ) {
        var hopperIntakeCommand = hopper.getIntakeCommand();
        this.addCommands(
                hopperIntakeCommand.alongWith(slowCollectorIntakeCommand).alongWith(shooterFeederFireCommand)
        );
    }
}