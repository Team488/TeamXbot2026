package competition.command_groups;

import xbot.common.command.BaseParallelCommandGroup;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.RPM;

public class MinHoodShootingCommandGroup extends BaseParallelCommandGroup {

    @Inject
    public MinHoodShootingCommandGroup(PrepareToShootCommandGroup prepareToShootCommandGroup
    ) {

        prepareToShootCommandGroup.setShooterGoal(RPM.of(4800));
        prepareToShootCommandGroup.setHoodGoal(0.0);

        this.addCommands(
                prepareToShootCommandGroup
        );
    }
}