package competition.command_groups;

import competition.subsystems.climber.commands.ClimberExtendCommand;
import competition.subsystems.climber.commands.ClimberRetractCommand;
import competition.subsystems.drive.commands.OutpostSideClimbAutoCommand;
import competition.subsystems.drive.commands.ReadyOutpostSideClimbAutoCommand;
import competition.subsystems.intake_deploy.commands.IntakeDeployRetractCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import xbot.common.command.BaseSequentialCommandGroup;

public class OutpostClimbCommandGroup extends BaseSequentialCommandGroup {
    public OutpostClimbCommandGroup(OutpostSideClimbAutoCommand outpostSideClimbAutoCommand,
                                    ReadyOutpostSideClimbAutoCommand readyOutpostSideClimbAutoCommand,
                                    ClimberExtendCommand climberExtendCommand,
                                    ClimberRetractCommand climberRetractCommand,
                                    IntakeDeployRetractCommand intakeDeployRetractCommand) {
        addCommands(
                readyOutpostSideClimbAutoCommand,
                new ParallelCommandGroup(
                        intakeDeployRetractCommand,
                        climberExtendCommand
                ),
                outpostSideClimbAutoCommand,
                climberRetractCommand
        );
    }
}
