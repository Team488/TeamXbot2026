package competition.command_groups;

import javax.inject.Inject;
import javax.inject.Provider;

import competition.auto_programs.ppl.PathPlanner;
import competition.auto_programs.AimAndShootFromHereCommand;
import competition.subsystems.collector_intake.commands.CollectorStopCommand;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;

public class DriveFromNeutralZoneToAllianceAndShootCommandGroupFactory {
    private final Provider<GetReadyForFiringCommandGroup> getReadyForFiringCommandGroup;
    private final Provider<CollectorStopCommand> collectorStopCommandProvider;
    private final PathPlanner pathPlanner;
    private final Provider<AimAndShootFromHereCommand> aimAndShootFromHereCommandProvider;

    @Inject
    public DriveFromNeutralZoneToAllianceAndShootCommandGroupFactory(
            Provider<GetReadyForFiringCommandGroup> getReadyForFiringCommandGroup,
            Provider<CollectorStopCommand> collectorStopCommandProvider,
            Provider<AimAndShootFromHereCommand> aimAndShootFromHereCommandProvider,
            PathPlanner pathPlanner) {
        this.getReadyForFiringCommandGroup = getReadyForFiringCommandGroup;
        this.collectorStopCommandProvider = collectorStopCommandProvider;
        this.aimAndShootFromHereCommandProvider = aimAndShootFromHereCommandProvider;
        this.pathPlanner = pathPlanner;
    }

    public Command create() {
        var driveFromNeutralToAlliance = Commands.deadline(
                pathPlanner.driveFromNeutralToAlliance(),
                this.collectorStopCommandProvider.get()
        ).withName("DriveFromNeutralToAllianceWithNoCollection");

        return Commands.sequence(
                driveFromNeutralToAlliance,
                this.getReadyForFiringCommandGroup.get(), // Why run this in auto though?
                this.aimAndShootFromHereCommandProvider.get()
        ).withName("DriveFromNeutralToAllianceAndShoot");
    }
}
