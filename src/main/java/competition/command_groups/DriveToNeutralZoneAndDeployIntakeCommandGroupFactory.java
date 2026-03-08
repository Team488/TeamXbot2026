package competition.command_groups;

import competition.subsystems.collector_intake.commands.CollectorIntakeCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.intake_deploy.commands.IntakeDeployExtendCommand;
import edu.wpi.first.wpilibj2.command.DeferredCommand;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import javax.inject.Inject;
import javax.inject.Provider;

import java.util.Set;

public class DriveToNeutralZoneAndDeployIntakeCommandGroupFactory {
    private final Provider<DriveToNeutralZoneForIntakeCommand> driveToNeutralZoneForIntakeCommandProvider;
    private final Provider<DriveAcrossMidNeutralZoneCommand> driveAcrossMidNeutralZoneCommandProvider;
    private final Provider<DriveAcrossTurnAroundToEndZoneCommand> driveAcrossTurnAroundToEndZoneCommandProvider;
    private final Provider<IntakeDeployExtendCommand> intakeDeployExtendCommandProvider;
    private final Provider<CollectorIntakeCommand> collectorIntakeCommandProvider;

    private final DriveSubsystem drive;

    @Inject
    public DriveToNeutralZoneAndDeployIntakeCommandGroupFactory(
            Provider<DriveToNeutralZoneForIntakeCommand> driveToNeutralZoneForIntakeCommandProvider,
            Provider<DriveAcrossMidNeutralZoneCommand> driveAcrossMidNeutralZoneCommandProvider,
            Provider<DriveAcrossTurnAroundToEndZoneCommand> driveAcrossTurnAroundToEndZoneCommandProvider,
            Provider<IntakeDeployExtendCommand> intakeDeployExtendCommandProvider,
            Provider<CollectorIntakeCommand> collectorIntakeCommandProvider,
            DriveSubsystem drive) {
        this.driveToNeutralZoneForIntakeCommandProvider = driveToNeutralZoneForIntakeCommandProvider;
        this.driveAcrossMidNeutralZoneCommandProvider = driveAcrossMidNeutralZoneCommandProvider;
        this.driveAcrossTurnAroundToEndZoneCommandProvider = driveAcrossTurnAroundToEndZoneCommandProvider;
        this.intakeDeployExtendCommandProvider = intakeDeployExtendCommandProvider;
        this.collectorIntakeCommandProvider = collectorIntakeCommandProvider;

        this.drive = drive;
    }

    public SequentialCommandGroup create() {
        var group = new SequentialCommandGroup();
        group.setName("DriveToNeutralZoneAndDeployIntakeCommandGroup");

        var driveToNeutral = new DeferredCommand(
                this.driveToNeutralZoneForIntakeCommandProvider::get, Set.of(drive));

        group.addCommands(driveToNeutral);

        var drivingForCollecting = new SequentialCommandGroup(this.driveAcrossMidNeutralZoneCommandProvider.get(),
                driveAcrossTurnAroundToEndZoneCommandProvider.get());

        var driveAcrossAndIntakeDeployCommandGroup = new ParallelDeadlineGroup(
                drivingForCollecting, intakeDeployExtendCommandProvider.get(), collectorIntakeCommandProvider.get());

        group.addCommands(driveAcrossAndIntakeDeployCommandGroup);

        return group;
    }

}
