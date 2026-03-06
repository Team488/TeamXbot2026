package competition.command_groups;

import xbot.common.command.BaseParallelCommandGroup;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.RPM;

public class MaxHoodShootingCommandGroup extends BaseParallelCommandGroup {

    @Inject
    public MaxHoodShootingCommandGroup(PrepareToShootCommandGroup prepareToShootCommandGroup
    ) {

        prepareToShootCommandGroup.setShooterGoal(RPM.of(4800));
        prepareToShootCommandGroup.setHoodGoal(1.0);

        this.addCommands(
                prepareToShootCommandGroup
        );
    }
}