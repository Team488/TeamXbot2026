package competition.command_groups;

import javax.inject.Inject;
import javax.inject.Provider;

import competition.subsystems.collector_intake.commands.CollectorStopCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.TrajectoriesCalculation;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

public class DriveFromNeutralZoneToAllianceAndShootCommandGroupFactory {
    private final Provider<DriveFromNeutralZoneToAllianceCommand> driveFromNeutralZoneToAllianceCommandProvider;
    private final Provider<GetReadyForFiringCommandGroup> getReadyForFiringCommandGroup;
    private final Provider<CollectorStopCommand> collectorStopCommandProvider;
    private final Provider<PrepareToShootCommandGroup> prepareToShootCommandGroupProvider;

    @Inject
    public DriveFromNeutralZoneToAllianceAndShootCommandGroupFactory(
            Provider<DriveFromNeutralZoneToAllianceCommand> driveFromNeutralZoneToAllianceCommandProvider,
            Provider<GetReadyForFiringCommandGroup> getReadyForFiringCommandGroup,
            Provider<CollectorStopCommand> collectorStopCommandProvider,
            DriveSubsystem drive,
            Provider<PrepareToShootCommandGroup> prepareToShootCommandGroupProvider) {
        this.driveFromNeutralZoneToAllianceCommandProvider = driveFromNeutralZoneToAllianceCommandProvider;
        this.getReadyForFiringCommandGroup = getReadyForFiringCommandGroup;
        this.collectorStopCommandProvider = collectorStopCommandProvider;
        this.prepareToShootCommandGroupProvider = prepareToShootCommandGroupProvider;
    }

    public SequentialCommandGroup create() {
        var group = new SequentialCommandGroup();
        group.setName("DriveFromNeutralZoneToAllianceAndShootCommandGroup");

        var prepareToShoot = prepareToShootCommandGroupProvider.get();
            prepareToShoot.setPresetLocation(TrajectoriesCalculation.PresetShootingDistance.TRENCH);
        
        var driveFromNeutralToAlliance = new ParallelDeadlineGroup(
                this.driveFromNeutralZoneToAllianceCommandProvider.get(), this.collectorStopCommandProvider.get(), prepareToShoot);
        group.addCommands(driveFromNeutralToAlliance);
        group.addCommands(this.getReadyForFiringCommandGroup.get());

        return group;
    }
}
