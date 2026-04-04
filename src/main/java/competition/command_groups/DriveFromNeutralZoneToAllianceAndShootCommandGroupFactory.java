package competition.command_groups;

import javax.inject.Inject;
import javax.inject.Provider;

import competition.auto_programs.AimAndShootFromHereCommand;
import competition.subsystems.collector_intake.commands.CollectorStopCommand;
import competition.subsystems.drive.DriveSubsystem;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

public class DriveFromNeutralZoneToAllianceAndShootCommandGroupFactory {
    private final Provider<DriveFromNeutralZoneToAllianceCommand> driveFromNeutralZoneToAllianceCommandProvider;
    private final Provider<PrepareToShootCommandGroup> prepareToShootCommandProvider;
    private final Provider<CollectorStopCommand> collectorStopCommandProvider;
    private final Provider<AimAndShootFromHereCommand> aimAndShootFromHereCommandProvider;

    @Inject
    public DriveFromNeutralZoneToAllianceAndShootCommandGroupFactory(
            Provider<DriveFromNeutralZoneToAllianceCommand> driveFromNeutralZoneToAllianceCommandProvider,
            Provider<PrepareToShootCommandGroup> prepareToShootCommandProvider,
            Provider<CollectorStopCommand> collectorStopCommandProvider,
            Provider<AimAndShootFromHereCommand> aimAndShootFromHereCommandProvider,
            DriveSubsystem drive) {
        this.driveFromNeutralZoneToAllianceCommandProvider = driveFromNeutralZoneToAllianceCommandProvider;
        this.prepareToShootCommandProvider = prepareToShootCommandProvider;
        this.collectorStopCommandProvider = collectorStopCommandProvider;
        this.aimAndShootFromHereCommandProvider = aimAndShootFromHereCommandProvider;
    }

    public SequentialCommandGroup create() {
        var group = new SequentialCommandGroup();
        group.setName("DriveFromNeutralZoneToAllianceAndShootCommandGroup");

        var driveFromNeutralToAlliance = new ParallelDeadlineGroup(
                this.driveFromNeutralZoneToAllianceCommandProvider.get(),
                this.collectorStopCommandProvider.get(),
                this.prepareToShootCommandProvider.get());
        group.addCommands(driveFromNeutralToAlliance);
        group.addCommands(this.aimAndShootFromHereCommandProvider.get());

        return group;
    }
}
