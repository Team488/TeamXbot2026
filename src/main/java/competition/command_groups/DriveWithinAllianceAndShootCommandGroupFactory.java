package competition.command_groups;

import competition.subsystems.drive.DriveSubsystem;
import edu.wpi.first.wpilibj2.command.DeferredCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import javax.inject.Inject;
import javax.inject.Provider;

import java.util.Set;

public class DriveWithinAllianceAndShootCommandGroupFactory {
    private final Provider<DriveFromNeutralZoneToAllianceCommand> driveFromNeutralZoneToAllianceCommandProvider;
    private final Provider<GetReadyForFiringCommandGroup> getReadyForFiringCommandGroup;

    private final DriveSubsystem drive;

    @Inject
    public DriveWithinAllianceAndShootCommandGroupFactory(
            Provider<DriveFromNeutralZoneToAllianceCommand> driveFromNeutralZoneToAllianceCommandProvider,
            Provider<GetReadyForFiringCommandGroup> getReadyForFiringCommandGroup,
            DriveSubsystem drive) {
        this.driveFromNeutralZoneToAllianceCommandProvider = driveFromNeutralZoneToAllianceCommandProvider;
        this.getReadyForFiringCommandGroup = getReadyForFiringCommandGroup;

        this.drive = drive;
    }

    public SequentialCommandGroup create() {
        var group = new SequentialCommandGroup();
        group.setName("DriveWithinAllianceAndShootCommandGroup");

        var getReadyForFiring = new DeferredCommand(
                this.getReadyForFiringCommandGroup::get, Set.of(drive));
        group.addCommands(getReadyForFiring);

        return group;
    }
}
