package competition.command_groups;

import competition.subsystems.climber.commands.ClimberExtendCommand;
import competition.subsystems.climber.commands.ClimberRetractCommand;
import competition.subsystems.drive.commands.MiddleClimbAutoCommand;
import competition.subsystems.drive.commands.OutpostSideClimbAutoCommand;
import competition.subsystems.drive.commands.ReadyMiddleClimbAutoCommand;
import competition.subsystems.drive.commands.ReadyOutpostSideClimbAutoCommand;
import competition.subsystems.intake_deploy.commands.IntakeDeployRetractCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import xbot.common.command.BaseSequentialCommandGroup;

import javax.inject.Inject;

public class MiddleClimbCommandGroup extends BaseSequentialCommandGroup {

    @Inject
    public MiddleClimbCommandGroup(MiddleClimbAutoCommand middleClimbAutoCommand,
                                   ReadyMiddleClimbAutoCommand readyMiddleClimbAutoCommand,
                                   ClimberExtendCommand climberExtendCommand,
                                   ClimberRetractCommand climberRetractCommand,
                                   IntakeDeployRetractCommand intakeDeployRetractCommand) {
        addCommands(
                readyMiddleClimbAutoCommand,
                new ParallelCommandGroup(
                        intakeDeployRetractCommand,
                        climberExtendCommand
                ),
                middleClimbAutoCommand,
                climberRetractCommand
        );
    }
}
