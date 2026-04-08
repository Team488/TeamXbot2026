package competition.command_groups;

import javax.inject.Inject;
import javax.inject.Provider;

import competition.auto_programs.AimAndShootFromHereCommand;
import competition.subsystems.collector_intake.commands.CollectorStopCommand;
import competition.subsystems.pose.TrajectoriesCalculation;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

public class DriveFromNeutralZoneToAllianceAndShootCommandGroupFactory {
    private final Provider<DriveFromNeutralZoneToAllianceCommand> driveFromNeutralZoneToAllianceCommandProvider;
    private final Provider<PrepareToShootCommandGroup> prepareToShootCommandProvider;
    private final Provider<CollectorStopCommand> collectorStopCommandProvider;
    private final Provider<AimAndShootFromHereCommand> aimAndShootFromHereCommandProvider;
    private final Provider<PrepareToShootCommandGroup> prepareToShootCommandGroupProvider;

    @Inject
    public DriveFromNeutralZoneToAllianceAndShootCommandGroupFactory(
            Provider<DriveFromNeutralZoneToAllianceCommand> driveFromNeutralZoneToAllianceCommandProvider,
            Provider<PrepareToShootCommandGroup> prepareToShootCommandProvider,
            Provider<CollectorStopCommand> collectorStopCommandProvider,
            Provider<AimAndShootFromHereCommand> aimAndShootFromHereCommandProvider,
            Provider<PrepareToShootCommandGroup> prepareToShootCommandGroupProvider) {
        this.driveFromNeutralZoneToAllianceCommandProvider = driveFromNeutralZoneToAllianceCommandProvider;
        this.prepareToShootCommandProvider = prepareToShootCommandProvider;
        this.collectorStopCommandProvider = collectorStopCommandProvider;
        this.aimAndShootFromHereCommandProvider = aimAndShootFromHereCommandProvider;
        this.prepareToShootCommandGroupProvider = prepareToShootCommandGroupProvider;
    }

    public SequentialCommandGroup create() {
        var group = new SequentialCommandGroup();
        group.setName("DriveFromNeutralZoneToAllianceAndShootCommandGroup");

        var driveFromNeutralToAlliance = new ParallelDeadlineGroup(
                this.driveFromNeutralZoneToAllianceCommandProvider.get(),
                this.collectorStopCommandProvider.get(),
                prepareToShootAtTrench()
        ).withName("DriveFromNeutralToAllianceAndWarmUpShooter");

        // Getting rid of the middle step such that we can immediately fire after
        // getting back to alliance from neutral zone.
        // group.addCommands(this.getReadyForFiringCommandGroup.get());

        group.addCommands(driveFromNeutralToAlliance, this.aimAndShootFromHereCommandProvider.get());
        return group;
    }

    public Command prepareToShootAtTrench() {
        var command = prepareToShootCommandGroupProvider.get();
        command.setPresetLocation(TrajectoriesCalculation.PresetShootingDistance.TRENCH);
        return command;
    }
}
