package competition.command_groups;

import xbot.common.command.BaseParallelCommandGroup;

import javax.inject.Inject;

public class FireWhenShooterReady extends BaseParallelCommandGroup {

    @Inject
    public FireWhenShooterReady(WaitForHoodAndShooterToBeAtGoalCommandGroup waitForHoodAndShooterToBeAtGoalCommandGroup,
                                RunCollectorHopperFeederCommandGroup runCollectorHopperFeederCommandGroup
    ) {
        this.addCommands(
                waitForHoodAndShooterToBeAtGoalCommandGroup
                        .andThen(runCollectorHopperFeederCommandGroup)
        );
    }
}