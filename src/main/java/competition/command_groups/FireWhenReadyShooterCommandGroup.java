package competition.command_groups;

import xbot.common.command.BaseParallelCommandGroup;

import javax.inject.Inject;

public class FireWhenReadyShooterCommandGroup extends BaseParallelCommandGroup {

    @Inject
    public FireWhenReadyShooterCommandGroup(WaitForHoodShooterGoalCommandGroup waitForHoodShooterGoalCommandGroup,
                                            RunCollectorHopperFeederCommandGroup runCollectorHopperFeederCommandGroup
    ) {
        this.addCommands(
                waitForHoodShooterGoalCommandGroup
                        .andThen(runCollectorHopperFeederCommandGroup)
        );
    }
}