package competition.command_groups;

import xbot.common.command.BaseSequentialCommandGroup;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.climber.commands.ClimberExtendCommand;
import competition.subsystems.climber.commands.ClimberRetractCommand;
import competition.subsystems.climber.commands.ClimberStopCommand;

import javax.inject.Inject;

public class AutoClimbCommandGroup extends BaseSequentialCommandGroup {

    @Inject
    public AutoClimbCommandGroup(DriveSubsystem drive, ClimberExtendCommand extendCommand,
                                 ClimberRetractCommand retractCommand, ClimberStopCommand stopCommand) {
        addCommands(
                extendCommand.getWaitForAtGoalCommand(),
                stopCommand,
//                drive,
                retractCommand
        );
    }
}
