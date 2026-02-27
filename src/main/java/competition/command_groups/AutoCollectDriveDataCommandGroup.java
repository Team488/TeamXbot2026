package competition.command_groups;

import competition.subsystems.drive.commands.DriveForwardForTime;
import xbot.common.command.BaseParallelCommandGroup;

import static edu.wpi.first.units.Units.Seconds;

public class AutoCollectDriveDataCommandGroup extends BaseParallelCommandGroup {
    public AutoCollectDriveDataCommandGroup (HopperAndIntakeCommandGroup hopperAndIntakeCommandGroup,
                                             DriveForwardForTime driveForwardForTime) {

        addCommands(hopperAndIntakeCommandGroup, driveForwardForTime.withTimeout(Seconds.of(1)));
    }
}
