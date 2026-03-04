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

        var waitForExtendCommand = climb.getWaitForAtGoalCommand();
        var waitForRetractCommand = climb.getWaitForAtGoalCommand();
        addCommands(
                extendCommand.alongWith(waitForExtendCommand),
                driveForward.setDuration(2.0).setPower(100),
                retractCommand.alongWith(waitForRetractCommand)
        );
    }
}
