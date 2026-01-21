package competition.command_groups;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import javax.inject.Inject;

public class DriveToOutpostCommandGroup extends SequentialCommandGroup {

    private final DriveSubsystem drive;
    private final PoseSubsystem pose;

    @Inject
    public DriveToOutpostCommandGroup(DriveSubsystem drive, PoseSubsystem pose) {
        this.drive = drive;
        this.pose = pose;

        this.addCommands(drive.run(3));


    }
}
