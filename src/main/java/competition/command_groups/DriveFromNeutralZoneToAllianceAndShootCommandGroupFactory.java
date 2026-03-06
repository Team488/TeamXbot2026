package competition.command_groups;

import competition.subsystems.drive.DriveSubsystem;
import edu.wpi.first.wpilibj2.command.DeferredCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import javax.inject.Inject;
import javax.inject.Provider;

import java.util.Set;

public class DriveFromNeutralZoneToAllianceAndShootCommandGroupFactory {
    private final Provider<DriveFromNeutralZoneToAllianceCommand> driveFromNeutralZoneToAllianceCommandProvider;
    private final Provider<DriveToShootingPositionCommand> driveToShootingPositionCommandProvider;

    private final DriveSubsystem drive;

    @Inject
    public DriveFromNeutralZoneToAllianceAndShootCommandGroupFactory(
            Provider<DriveFromNeutralZoneToAllianceCommand> driveFromNeutralZoneToAllianceCommandProvider,
            Provider<DriveToShootingPositionCommand> driveToShootingPositionCommandProvider,
            DriveSubsystem drive) {
        this.driveFromNeutralZoneToAllianceCommandProvider = driveFromNeutralZoneToAllianceCommandProvider;
        this.driveToShootingPositionCommandProvider = driveToShootingPositionCommandProvider;

        this.drive = drive;
    }

    public SequentialCommandGroup create() {
        var group = new SequentialCommandGroup();
        group.setName("DriveToNeutralZoneAndDeployIntakeCommandGroup");

        var driveToAlliance = new DeferredCommand(
                this.driveFromNeutralZoneToAllianceCommandProvider::get, Set.of(drive));
        group.addCommands(driveToAlliance);

        var driveToShootingPosition = new DeferredCommand(this.driveToShootingPositionCommandProvider::get, Set.of(drive));
        group.addCommands(driveToShootingPosition);

        return group;
    }

}
