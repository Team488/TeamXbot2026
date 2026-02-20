package competition.command_groups;

import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import competition.subsystems.shooter_feeder.commands.ShooterFeederEject;
import xbot.common.command.BaseParallelCommandGroup;

import javax.inject.Inject;

public class EjectWhenShooterReadyCommandGroup extends BaseParallelCommandGroup {

    @Inject
    public EjectWhenShooterReadyCommandGroup(ShooterFeederEject shooterFeederEjectCommand, HopperRollerSubsystem hopper)
    {
        this.addCommands(
                hopper.getEjectCommand(),
                shooterFeederEjectCommand);
    }
}