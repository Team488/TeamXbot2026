package competition.command_groups;

import competition.general_commands.WaitForDurationCommand;
import xbot.common.command.BaseSequentialCommandGroup;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

public class GetReadyForFiringCommandGroup extends BaseSequentialCommandGroup {

    @Inject
    public GetReadyForFiringCommandGroup(DriveToShootingPositionCommand driveToShootingPositionCommand,
        WaitForHoodAndShooterToBeAtGoalCommandGroup waitForHoodAndShooterToBeAtGoalCommandGroup,
                                       RunCollectorHopperFeederCommandGroup runCollectorHopperFeederCommandGroup,
                                       PropertyFactory pf
    ) {
        pf.setPrefix(this);
        var shootingTimeoutSeconds = pf.createPersistentProperty("Shooting timeout seconds", 3.0);
        
        this.addCommands(driveToShootingPositionCommand);

        var fireWhenShooterAndHoodReady = waitForHoodAndShooterToBeAtGoalCommandGroup
                .andThen(new WaitForDurationCommand(shootingTimeoutSeconds::get).deadlineFor(runCollectorHopperFeederCommandGroup));
        
        this.addCommands(fireWhenShooterAndHoodReady);
    }
}
