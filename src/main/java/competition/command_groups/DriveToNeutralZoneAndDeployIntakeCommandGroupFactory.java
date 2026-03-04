package competition.command_groups;

import competition.subsystems.drive.DriveSubsystem;
import edu.wpi.first.wpilibj2.command.DeferredCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import javax.inject.Inject;
import javax.inject.Provider;

import java.util.Set;

public class DriveToNeutralZoneAndDeployIntakeCommandGroupFactory {
    private final Provider<DriveToNeutralZoneForIntakeCommand> driveToNeutralZoneForIntakeCommandProvider;
    private final DriveSubsystem drive;

    @Inject
    public DriveToNeutralZoneAndDeployIntakeCommandGroupFactory(
            Provider<DriveToNeutralZoneForIntakeCommand> driveToNeutralZoneForIntakeCommandProvider,
            Provider<DriveAcrossNeutralZoneCommand> driveAcrossNeutralZoneCommandProvider,
            DriveSubsystem drive) {
        this.driveToNeutralZoneForIntakeCommandProvider = driveToNeutralZoneForIntakeCommandProvider;
        this.drive = drive;
    }

    public SequentialCommandGroup create() {
        var group = new SequentialCommandGroup();
        group.setName("DriveToNeutralZoneAndDeployIntakeCommandGroup");

        var driveToNeutral = new DeferredCommand(
                () -> {
                    var driveToNeutralZoneForIntakeCommand = this.driveToNeutralZoneForIntakeCommandProvider.get();
                    return driveToNeutralZoneForIntakeCommand;
                }, Set.of(drive)
        );
        group.addCommands(driveToNeutral);

        return group;
    }
}
