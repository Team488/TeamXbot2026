package competition.command_groups;


import competition.general_commands.WaitForDurationCommand;
import competition.subsystems.intake_deploy.commands.IntakeDeployAdaptiveCloseWhileFiringCommand;
import edu.wpi.first.wpilibj2.command.Commands;
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
                                        IntakeDeployAdaptiveCloseWhileFiringCommand intakeDeployOscillating,
                                        PropertyFactory propertyFactory

    ) {
        propertyFactory.setPrefix(this);

        this.addCommands(
                waitForHoodAndShooterToBeAtGoalCommandGroup,
                Commands.parallel(
                        runCollectorHopperFeederCommandGroup,
                        Commands.sequence(
                                intakeDeployOscillating
                        )
                )
        );
    }
}
