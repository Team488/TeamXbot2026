package competition.command_groups;

import xbot.common.command.BaseSequentialCommandGroup;
import competition.subsystems.drive.commands.DriveForwardToClimbCommand;
import competition.subsystems.climber.ClimberSubsystem;
import competition.subsystems.climber.commands.ClimberExtendCommand;
import competition.subsystems.climber.commands.ClimberRetractCommand;

import javax.inject.Inject;

public class AutoClimbCommandGroup extends BaseSequentialCommandGroup {

    @Inject
    public AutoClimbCommandGroup(ClimberSubsystem climb, ClimberExtendCommand extendCommand,
                                 ClimberRetractCommand retractCommand, DriveForwardToClimbCommand driveForward) {

        var extendWaitCommand = climb.getWaitForAtGoalCommand();
        var retractWaitCommand = climb.getWaitForAtGoalCommand();
        addCommands(
                extendCommand.alongWith(extendWaitCommand),
                driveForward.setDuration(2.0).setPower(1),
                retractCommand.alongWith(retractWaitCommand)
        );
    }
}
