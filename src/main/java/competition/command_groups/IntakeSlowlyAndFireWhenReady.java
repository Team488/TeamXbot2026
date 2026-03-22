package competition.command_groups;


import competition.subsystems.intake_deploy.commands.IntakeDeploySlowClosing;
import xbot.common.command.BaseSequentialCommandGroup;

import javax.inject.Inject;

public class IntakeSlowlyAndFireWhenReady extends BaseSequentialCommandGroup {

    @Inject
    public IntakeSlowlyAndFireWhenReady(WaitForHoodAndShooterToBeAtGoalCommandGroup waitForHoodAndShooterToBeAtGoalCommandGroup,
                                       RunCollectorHopperFeederCommandGroup runCollectorHopperFeederCommandGroup,
                                       IntakeDeploySlowClosing intakeDeploySlowClosing
    ) {
        this.addCommands(
                waitForHoodAndShooterToBeAtGoalCommandGroup,
                runCollectorHopperFeederCommandGroup.alongWith(intakeDeploySlowClosing));
    }
}
