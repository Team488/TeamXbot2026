package competition.command_groups;

import competition.subsystems.collector_intake.commands.CollectorIntakeCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.intake_deploy.commands.IntakeDeployExtendCommand;
import edu.wpi.first.wpilibj2.command.DeferredCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import javax.inject.Inject;
import javax.inject.Provider;

import java.util.Set;

public class DriveToNeutralZoneAndDeployIntakeCommandGroupFactory {
    private final Provider<DriveToNeutralZoneForIntakeCommand> driveToNeutralZoneForIntakeCommandProvider;
    private final Provider<DriveAcrossMidNeutralZoneCommand> driveAcrossMidNeutralZoneCommandProvider;
    private final Provider<IntakeDeployExtendCommand> intakeDeployExtendCommandProvider;
    private final Provider<CollectorIntakeCommand> collectorIntakeCommandProvider;

    private final DriveSubsystem drive;

    @Inject
    public DriveToNeutralZoneAndDeployIntakeCommandGroupFactory(
            Provider<DriveToNeutralZoneForIntakeCommand> driveToNeutralZoneForIntakeCommandProvider,
            Provider<DriveAcrossMidNeutralZoneCommand> driveAcrossMidNeutralZoneCommandProvider,
            Provider<IntakeDeployExtendCommand> intakeDeployExtendCommandProvider,
            Provider<CollectorIntakeCommand> collectorIntakeCommandProvider,
            DriveSubsystem drive) {
        this.driveToNeutralZoneForIntakeCommandProvider = driveToNeutralZoneForIntakeCommandProvider;
        this.driveAcrossMidNeutralZoneCommandProvider = driveAcrossMidNeutralZoneCommandProvider;
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

        var driveAcrossAndIntakeDeployCommandGroup = new ParallelCommandGroup(
                this.driveAcrossMidNeutralZoneCommandProvider.get(), intakeDeployExtendCommandProvider.get());

        var driveAcrossIntakeDeployWithFuelIntakeCommand = new ParallelDeadlineGroup(
                driveAcrossAndIntakeDeployCommandGroup, collectorIntakeCommandProvider.get());

        group.addCommands(driveAcrossIntakeDeployWithFuelIntakeCommand);

        return group;
    }

}
