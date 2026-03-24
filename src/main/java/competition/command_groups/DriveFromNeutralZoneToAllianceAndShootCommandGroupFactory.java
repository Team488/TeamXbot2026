package competition.command_groups;

import javax.inject.Inject;
import javax.inject.Provider;

import competition.subsystems.collector_intake.commands.CollectorStopCommand;
import competition.subsystems.drive.DriveSubsystem;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.command.BaseParallelDeadlineGroup;
import xbot.common.command.BaseSequentialCommandGroup;

public class DriveFromNeutralZoneToAllianceAndShootCommandGroupFactory {
    private final Provider<DriveFromNeutralZoneToAllianceCommand> driveFromNeutralZoneToAllianceCommandProvider;
    private final Provider<GetReadyForFiringCommandGroup> getReadyForFiringCommandGroup;
    private final Provider<CollectorStopCommand> collectorStopCommandProvider;

    @Inject
    public DriveFromNeutralZoneToAllianceAndShootCommandGroupFactory(
            Provider<DriveFromNeutralZoneToAllianceCommand> driveFromNeutralZoneToAllianceCommandProvider,
            Provider<GetReadyForFiringCommandGroup> getReadyForFiringCommandGroup,
            Provider<CollectorStopCommand> collectorStopCommandProvider,
            DriveSubsystem drive) {
        this.driveFromNeutralZoneToAllianceCommandProvider = driveFromNeutralZoneToAllianceCommandProvider;
        this.getReadyForFiringCommandGroup = getReadyForFiringCommandGroup;
        this.collectorStopCommandProvider = collectorStopCommandProvider;
    }

    public BaseSequentialCommandGroup create() {
        var group = new BaseSequentialCommandGroup();
        group.setName("DriveFromNeutralZoneToAllianceAndShootCommandGroup");

        var driveFromNeutralToAlliance = new BaseParallelDeadlineGroup(
                this.driveFromNeutralZoneToAllianceCommandProvider.get(), this.collectorStopCommandProvider.get());
        group.addCommands(driveFromNeutralToAlliance);
        group.addCommands(this.getReadyForFiringCommandGroup.get());

        return group;
    }
}
