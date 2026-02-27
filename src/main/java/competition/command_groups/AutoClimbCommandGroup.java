package competition.command_groups;

import xbot.common.command.BaseSequentialCommandGroup;
import competition.subsystems.drive.commands.DriveForwardToClimbCommand;
import competition.subsystems.climber.commands.ClimberExtendCommand;
import competition.subsystems.climber.commands.ClimberRetractCommand;
import competition.subsystems.climber.commands.ClimberStopCommand;

import javax.inject.Inject;

public class AutoClimbCommandGroup extends BaseSequentialCommandGroup {

    @Inject
    public AutoClimbCommandGroup(DriveForwardToClimbCommand drive, ClimberExtendCommand extendCommand,
                                 ClimberRetractCommand retractCommand, ClimberStopCommand stopCommand) {
        addCommands(
                extendCommand.withTimeout(2.0),
                stopCommand,
                drive,
                retractCommand
        );
    }
}
