package competition.command_groups;

import javax.inject.Inject;
import javax.inject.Provider;

import competition.auto_programs.ppl.PathPlanner;
import competition.auto_programs.AimAndShootFromHereCommand;
import competition.subsystems.collector_intake.commands.CollectorStopCommand;
import competition.subsystems.pose.TrajectoriesCalculation;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

public class DriveFromNeutralZoneToAllianceAndShootCommandGroupFactory {
    private final Provider<CollectorStopCommand> collectorStopCommandProvider;
    private final PathPlanner pathPlanner;
    private final Provider<AimAndShootFromHereCommand> aimAndShootFromHereCommandProvider;
    private final Provider<PrepareToShootCommandGroup> prepareToShootCommandGroupProvider;

    @Inject
    public DriveFromNeutralZoneToAllianceAndShootCommandGroupFactory(
            Provider<CollectorStopCommand> collectorStopCommandProvider,
            Provider<AimAndShootFromHereCommand> aimAndShootFromHereCommandProvider,
            Provider<PrepareToShootCommandGroup> prepareToShootCommandGroupProvider,
            PathPlanner pathPlanner) {
        this.collectorStopCommandProvider = collectorStopCommandProvider;
        this.aimAndShootFromHereCommandProvider = aimAndShootFromHereCommandProvider;
        this.prepareToShootCommandGroupProvider = prepareToShootCommandGroupProvider;
        this.pathPlanner = pathPlanner;
    }

    public Command create() {
        var group = new SequentialCommandGroup();
        group.setName("DriveFromNeutralZoneToAllianceAndShootCommandGroup");

        var driveFromNeutralToAlliance = new ParallelDeadlineGroup(
                pathPlanner.driveFromNeutralToAlliance(),
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
