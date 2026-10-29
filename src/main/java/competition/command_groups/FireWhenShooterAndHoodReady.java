package competition.command_groups;

import xbot.common.command.BaseParallelCommandGroup;

import javax.inject.Inject;

public class FireWhenShooterAndHoodReady extends BaseParallelCommandGroup {

    @Inject
    public FireWhenShooterAndHoodReady(WaitForHoodAndShooterToBeAtGoalCommandGroup waitForHoodAndShooterToBeAtGoalCommandGroup,
                                       RunCollectorHopperFeederCommandGroup runCollectorHopperFeederCommandGroup
    ) {
        this.addCommands(
                waitForHoodAndShooterToBeAtGoalCommandGroup
                        .andThen(runCollectorHopperFeederCommandGroup)
        );
    }
}