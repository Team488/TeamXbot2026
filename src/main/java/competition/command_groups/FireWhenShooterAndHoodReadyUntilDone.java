package competition.command_groups;

import competition.subsystems.shooter_feeder.commands.WaitForShootingFinished;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;
import xbot.common.command.BaseParallelCommandGroup;

import javax.inject.Inject;

public class FireWhenShooterAndHoodReadyUntilDone extends BaseParallelCommandGroup {

    @Inject
    public FireWhenShooterAndHoodReadyUntilDone(WaitForHoodAndShooterToBeAtGoalCommandGroup waitForHoodAndShooterToBeAtGoalCommandGroup,
                                                WaitForShootingFinished waitForShootingFinished,
                                                RunCollectorHopperFeederCommandGroup runCollectorHopperFeederCommandGroup
    ) {
        var shootUntilDone = new ParallelDeadlineGroup(waitForShootingFinished, runCollectorHopperFeederCommandGroup);
        shootUntilDone.setName("FireWhenShooterAndHoodReadyUntilDone - Shoot Until Done");

        this.addCommands(
                waitForHoodAndShooterToBeAtGoalCommandGroup
                        .andThen(shootUntilDone)
        );
    }
}