package competition.command_groups;

import competition.subsystems.drive.DriveSubsystem;
import edu.wpi.first.wpilibj2.command.DeferredCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import javax.inject.Inject;
import javax.inject.Provider;

import java.util.Set;

public class DriveFromNeutralZoneToAllianceAndShootCommandGroupFactory {
    private final Provider<DriveThroughTrenchToNeutralZoneCommand> driveThroughTrenchToNeutralZoneCommandProvider;

    private final DriveSubsystem drive;

    @Inject
    public DriveFromNeutralZoneToAllianceAndShootCommandGroupFactory(
            Provider<DriveThroughTrenchToNeutralZoneCommand> driveThroughTrenchToNeutralZoneCommandProvider,
            DriveSubsystem drive) {
        this.driveThroughTrenchToNeutralZoneCommandProvider = driveThroughTrenchToNeutralZoneCommandProvider;

        this.drive = drive;
    }

    public SequentialCommandGroup create() {
        var group = new SequentialCommandGroup();
        group.setName("DriveToNeutralZoneAndDeployIntakeCommandGroup");

        var driveToAlliance = new DeferredCommand(
                this.driveThroughTrenchToNeutralZoneCommandProvider::get, Set.of(drive));

        group.addCommands(driveToAlliance);

        return group;
    }

}
