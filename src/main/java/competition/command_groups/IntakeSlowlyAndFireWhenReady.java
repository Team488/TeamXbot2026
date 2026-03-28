package competition.command_groups;


import competition.general_commands.WaitForDurationCommand;
import competition.subsystems.intake_deploy.commands.IntakeDeploySlowClosing;
import xbot.common.command.BaseSequentialCommandGroup;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

public class IntakeSlowlyAndFireWhenReady extends BaseSequentialCommandGroup {

    @Inject
    public IntakeSlowlyAndFireWhenReady(WaitForHoodAndShooterToBeAtGoalCommandGroup waitForHoodAndShooterToBeAtGoalCommandGroup,
                                       RunCollectorHopperFeederCommandGroup runCollectorHopperFeederCommandGroup,
                                       IntakeDeploySlowClosing intakeDeploySlowClosing,
                                       PropertyFactory propertyFactory

    ) {
        propertyFactory.setPrefix(this);

        var waitBeforeRetracting = new WaitForDurationCommand(
                propertyFactory.createPersistentProperty
                        ("waitBeforeRetractingSeconds", 2.0)::get
        );

        this.addCommands(
                waitForHoodAndShooterToBeAtGoalCommandGroup,
                runCollectorHopperFeederCommandGroup
 .alongWith(waitBeforeRetracting.andThen(intakeDeploySlowClosing))
                                .andThen(intakeDeploySlowClosing)
        );
    }
}
