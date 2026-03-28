package competition.command_groups;

import competition.subsystems.collector_intake.commands.CollectorEjectCommand;
import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import competition.subsystems.shooter_feeder.commands.ShooterFeederEject;
import xbot.common.command.BaseParallelCommandGroup;

import javax.inject.Inject;

public class HopperAndIntakeEjectCommandGroup extends BaseParallelCommandGroup {

    @Inject
    public HopperAndIntakeEjectCommandGroup(HopperRollerSubsystem hopperRoller,
                                            CollectorEjectCommand fuelEjectCommand,
                                            ShooterFeederEject shooterFeederEject) {
        addCommands(fuelEjectCommand, hopperRoller.getEjectCommand(), shooterFeederEject);
    }
}
