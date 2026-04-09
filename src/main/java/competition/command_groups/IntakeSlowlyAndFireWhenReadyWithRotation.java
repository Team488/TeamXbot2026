package competition.command_groups;


import competition.general_commands.WaitForDurationCommand;
import competition.subsystems.intake_deploy.commands.IntakeDeployOscillating;
import competition.subsystems.intake_deploy.commands.IntakeDeploySlowClosing;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import xbot.common.command.BaseSequentialCommandGroup;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

public class IntakeSlowlyAndFireWhenReadyWithRotation extends BaseSequentialCommandGroup {

    public DoubleProperty waitBeforeRetracting;

    @Inject
    public IntakeSlowlyAndFireWhenReadyWithRotation(
                                        WaitForRotationAndHoodAndShooterToBeAtGoalCommandGroup waitForRotationAndHoodAndShooterToBeAtGoalCommandGroup,
                                        RunCollectorHopperFeederCommandGroup runCollectorHopperFeederCommandGroup,
                                        IntakeDeployOscillating intakeDeployOscillating,
                                        PropertyFactory propertyFactory

    ) {
        propertyFactory.setPrefix(this);

        var waitBeforeRetracting = new WaitForDurationCommand(
                propertyFactory.createPersistentProperty
                        ("waitBeforeRetractingSeconds", 2.0)::get
        );

        this.addCommands(
                waitForRotationAndHoodAndShooterToBeAtGoalCommandGroup,
                Commands.parallel(
                        runCollectorHopperFeederCommandGroup,
                        Commands.sequence(
                                waitBeforeRetracting,
                                intakeDeployOscillating
                        )
                )
        );
    }
}
