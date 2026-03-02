package competition.command_groups;

import competition.subsystems.drive.DriveSubsystem;
import edu.wpi.first.wpilibj2.command.DeferredCommand;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import javax.inject.Inject;
import javax.inject.Provider;

import java.util.Set;

public class DriveToNeutralZoneAndDeployIntakeCommandGroupFactory {
    private final Provider<DriveToNeutralZoneForIntakeCommand> driveToNeutralZoneForIntakeCommandProvider;
    private final Provider<DriveAcrossNeutralZoneCommand> driveAcrossNeutralZoneCommandProvider;
    private final DriveSubsystem drive;

    @Inject
    public DriveToNeutralZoneAndDeployIntakeCommandGroupFactory(
            Provider<DriveToNeutralZoneForIntakeCommand> driveToNeutralZoneForIntakeCommandProvider,
            Provider<DriveAcrossNeutralZoneCommand> driveAcrossNeutralZoneCommand,
            DriveSubsystem drive) {
        this.driveToNeutralZoneForIntakeCommandProvider = driveToNeutralZoneForIntakeCommandProvider;
        this.driveAcrossNeutralZoneCommandProvider = driveAcrossNeutralZoneCommand;
        this.drive = drive;
    }

    public SequentialCommandGroup create() {
        return new SequentialCommandGroup(
                        new DeferredCommand(this.driveToNeutralZoneForIntakeCommandProvider::get, Set.of(drive)),
                        new DeferredCommand(this.driveAcrossNeutralZoneCommandProvider::get, Set.of(drive))
        );
    }
}
