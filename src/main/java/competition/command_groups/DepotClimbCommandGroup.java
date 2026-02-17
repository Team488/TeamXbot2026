package competition.command_groups;

import competition.subsystems.climber.commands.ClimberExtendCommand;
import competition.subsystems.climber.commands.ClimberRetractCommand;
import competition.subsystems.drive.commands.DepotSideClimbAutoCommand;
import competition.subsystems.drive.commands.OutpostSideClimbAutoCommand;
import competition.subsystems.drive.commands.ReadyDepotSideClimbAutoCommand;
import competition.subsystems.drive.commands.ReadyOutpostSideClimbAutoCommand;
import xbot.common.command.BaseSequentialCommandGroup;

public class DepotClimbCommandGroup extends BaseSequentialCommandGroup {
    public DepotClimbCommandGroup(DepotSideClimbAutoCommand depotSideClimbAutoCommand,
                                  ReadyDepotSideClimbAutoCommand readyDepotSideClimbAutoCommand,
                                  ClimberExtendCommand climberExtendCommand,
                                  ClimberRetractCommand climberRetractCommand) {
        addCommands(
                readyDepotSideClimbAutoCommand,
                climberExtendCommand,
                depotSideClimbAutoCommand,
                climberRetractCommand
        );
    }
}
