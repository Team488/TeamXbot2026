package competition.command_groups;

import competition.subsystems.drive.commands.DriveForwardCommand;
import xbot.common.command.BaseParallelCommandGroup;

import static edu.wpi.first.units.Units.Seconds;

public class AutoCollectDriveDataCommandGroup extends BaseParallelCommandGroup {
    public AutoCollectDriveDataCommandGroup (HopperAndIntakeCommandGroup hopperAndIntakeCommandGroup,
                                             DriveForwardCommand driveForwardForTime) {

        addCommands(hopperAndIntakeCommandGroup, driveForwardForTime.withTimeout(Seconds.of(1)));
    }
}
