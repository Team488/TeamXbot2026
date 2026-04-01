package competition.command_groups;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.DriveToOutpostCommand;
import competition.subsystems.pose.Landmarks;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import xbot.common.command.BaseParallelCommandGroup;

import javax.inject.Inject;


public class StartAndDriveToOutpostCommandGroup extends BaseParallelCommandGroup {
    @Inject
    public StartAndDriveToOutpostCommandGroup(DriveToOutpostCommand driveToOutpostCommand, PoseSubsystem pose) {
        addCommands(
                pose.createSetPositionCommand(PoseSubsystem.convertBlueToRedIfNeeded(Landmarks.blueStartTrenchToOutpost)),
                driveToOutpostCommand
        );
    }
}
