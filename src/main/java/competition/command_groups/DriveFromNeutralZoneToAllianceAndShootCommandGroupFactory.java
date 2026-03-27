package competition.command_groups;

import javax.inject.Inject;
import javax.inject.Provider;

import competition.auto_programs.ppl.PathPlanner;
import competition.subsystems.collector_intake.commands.CollectorStopCommand;
import competition.subsystems.drive.DriveSubsystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

public class DriveFromNeutralZoneToAllianceAndShootCommandGroupFactory {
    private final Provider<GetReadyForFiringCommandGroup> getReadyForFiringCommandGroup;
    private final Provider<CollectorStopCommand> collectorStopCommandProvider;
    private final PathPlanner pathPlanner;

    @Inject
    public DriveFromNeutralZoneToAllianceAndShootCommandGroupFactory(
            Provider<GetReadyForFiringCommandGroup> getReadyForFiringCommandGroup,
            Provider<CollectorStopCommand> collectorStopCommandProvider,
            PathPlanner pathPlanner) {
        this.getReadyForFiringCommandGroup = getReadyForFiringCommandGroup;
        this.collectorStopCommandProvider = collectorStopCommandProvider;
        this.pathPlanner = pathPlanner;
    }

    public Command create() {
        var driveFromNeutralToAlliance = Commands.deadline(
                pathPlanner.driveFromNeutralToAlliance(),
                this.collectorStopCommandProvider.get()
        ).withName("DriveFromNeutralToAllianceWithNoCollection");

        return Commands.sequence(
                driveFromNeutralToAlliance,
                this.getReadyForFiringCommandGroup.get() // Why run this in auto though?
        ).withName("DriveFromNeutralToAllianceAndShoot");
    }
}
