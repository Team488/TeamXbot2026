package competition.command_groups;

import competition.auto_programs.AimAndShootFromHereCommand;
import competition.subsystems.drive.commands.DriveToOutpostCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import xbot.common.command.BaseSequentialCommandGroup;

import javax.inject.Inject;


public class AutoOutpostAndShootCommandGroup extends BaseSequentialCommandGroup {

    @Inject
    public AutoOutpostAndShootCommandGroup(StartAndDriveToOutpostCommandGroup startAndDriveToOutpostCommandGroup,
                                           DriveToShootingPositionCommand driveToShootingPositionCommand,
                                           AimAndShootFromHereCommand aimAndShootFromHereCommand
                                           ){
        addCommands(startAndDriveToOutpostCommandGroup,
                withTimeout(5),
                driveToShootingPositionCommand,
                aimAndShootFromHereCommand
                );
    }

}
