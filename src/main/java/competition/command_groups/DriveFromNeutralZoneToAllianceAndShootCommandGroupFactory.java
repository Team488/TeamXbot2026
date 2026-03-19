package competition.command_groups;

import competition.subsystems.drive.DriveSubsystem;
import edu.wpi.first.wpilibj2.command.DeferredCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import javax.inject.Inject;
import javax.inject.Provider;

import java.util.Set;

public class DriveFromNeutralZoneToAllianceAndShootCommandGroupFactory {
    private final Provider<DriveFromNeutralZoneToAllianceCommand> driveFromNeutralZoneToAllianceCommandProvider;
    private final Provider<GetReadyForFiringCommandGroup> getReadyForFiringCommandGroup;

    @Inject
    public DriveFromNeutralZoneToAllianceAndShootCommandGroupFactory(
            Provider<DriveFromNeutralZoneToAllianceCommand> driveFromNeutralZoneToAllianceCommandProvider,
            Provider<GetReadyForFiringCommandGroup> getReadyForFiringCommandGroup,
            DriveSubsystem drive) {
        this.driveFromNeutralZoneToAllianceCommandProvider = driveFromNeutralZoneToAllianceCommandProvider;
        this.getReadyForFiringCommandGroup = getReadyForFiringCommandGroup;
    }

    public SequentialCommandGroup create() {
        var group = new SequentialCommandGroup();
        group.setName("DriveFromNeutralZoneToAllianceAndShootCommandGroup");

        group.addCommands(this.driveFromNeutralZoneToAllianceCommandProvider.get());
        group.addCommands(this.getReadyForFiringCommandGroup.get());

        return group;
    }
}
