package competition.command_groups;

import xbot.common.command.BaseParallelCommandGroup;

import javax.inject.Inject;

public class FireWhenShooterReady extends BaseParallelCommandGroup {

    @Inject
    public FireWhenShooterReady(WaitForHoodShooterGoalCommandGroup waitForHoodShooterGoalCommandGroup,
                                RunCollectorHopperFeederCommandGroup runCollectorHopperFeederCommandGroup
    ) {
        this.addCommands(
                waitForHoodShooterGoalCommandGroup
                        .andThen(runCollectorHopperFeederCommandGroup)
        );
    }
}