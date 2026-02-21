package competition.command_groups;

import xbot.common.command.BaseParallelCommandGroup;

import javax.inject.Inject;

public class EjectAllCommandGroup extends BaseParallelCommandGroup {

    @Inject
    public EjectAllCommandGroup(EjectWhenShooterReadyCommandGroup ejectWhenShooterReadyCommandGroup,
                                HopperAndIntakeEjectCommandGroup hopperAndIntakeEjectCommandGroup) {
        addCommands(ejectWhenShooterReadyCommandGroup,
                hopperAndIntakeEjectCommandGroup);
    }

}
