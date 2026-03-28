package competition.command_groups;


import competition.subsystems.intake_deploy.commands.IntakeDeploySlowClosing;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import xbot.common.command.BaseSequentialCommandGroup;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

public class IntakeSlowlyAndFireWhenReady extends BaseSequentialCommandGroup {

    public DoubleProperty waitBeforeRetracting;

    @Inject
    public IntakeSlowlyAndFireWhenReady(WaitForHoodAndShooterToBeAtGoalCommandGroup waitForHoodAndShooterToBeAtGoalCommandGroup,
                                       RunCollectorHopperFeederCommandGroup runCollectorHopperFeederCommandGroup,
                                       IntakeDeploySlowClosing intakeDeploySlowClosing,
                                       PropertyFactory propertyFactory

    ) {

        this.waitBeforeRetracting = propertyFactory.createPersistentProperty("Wait Time Before Retracting", 2.0);

        this.addCommands(
                waitForHoodAndShooterToBeAtGoalCommandGroup,
                runCollectorHopperFeederCommandGroup
                        .alongWith(new WaitCommand(waitBeforeRetracting.get())
                                .andThen(intakeDeploySlowClosing))
        );
    }
}
