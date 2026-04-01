package competition.command_groups;

import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Provider;

import competition.subsystems.collector_intake.commands.CollectorStopCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.TrajectoriesCalculation;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

public class DriveFromNeutralZoneToAllianceAndShootCommandGroupFactory {
    private final Provider<DriveFromNeutralZoneToAllianceCommand> driveFromNeutralZoneToAllianceCommandProvider;
    private final DriveToNearestShootingPositionAndShootWhenReady.Factory driveToNearestShootingPositionAndShootFactory;
    private final Provider<CollectorStopCommand> collectorStopCommandProvider;
    private final Provider<PrepareToShootCommandGroup> prepareToShootCommandGroupProvider;

    @Inject
    public DriveFromNeutralZoneToAllianceAndShootCommandGroupFactory(
            Provider<DriveFromNeutralZoneToAllianceCommand> driveFromNeutralZoneToAllianceCommandProvider,
            DriveToNearestShootingPositionAndShootWhenReady.Factory driveToNearestShootingPositionAndShootFactory,
            Provider<CollectorStopCommand> collectorStopCommandProvider,
            DriveSubsystem drive,
            Provider<PrepareToShootCommandGroup> prepareToShootCommandGroupProvider) {
        this.driveFromNeutralZoneToAllianceCommandProvider = driveFromNeutralZoneToAllianceCommandProvider;
        this.driveToNearestShootingPositionAndShootFactory = driveToNearestShootingPositionAndShootFactory;
        this.collectorStopCommandProvider = collectorStopCommandProvider;
        this.prepareToShootCommandGroupProvider = prepareToShootCommandGroupProvider;
    }

    public SequentialCommandGroup create() {
        return create(() -> Double.MAX_VALUE);
    }

    public SequentialCommandGroup create(Supplier<Double> shootingTimeoutSeconds) {
        var group = new SequentialCommandGroup();
        group.setName("DriveFromNeutralZoneToAllianceAndShootCommandGroup");

        var prepareToShoot = prepareToShootCommandGroupProvider.get();
            prepareToShoot.setPresetLocation(TrajectoriesCalculation.PresetShootingDistance.TRENCH);
        
        var driveFromNeutralToAlliance = new ParallelDeadlineGroup(
                this.driveFromNeutralZoneToAllianceCommandProvider.get(), this.collectorStopCommandProvider.get(), prepareToShoot);
        group.addCommands(driveFromNeutralToAlliance);
        group.addCommands(this.driveToNearestShootingPositionAndShootFactory.create(shootingTimeoutSeconds));

        return group;
    }
}
