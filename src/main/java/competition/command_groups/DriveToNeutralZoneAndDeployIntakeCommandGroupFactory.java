package competition.command_groups;

import javax.inject.Inject;
import javax.inject.Provider;

import competition.subsystems.collector_intake.commands.CollectorIntakeCommand;
import competition.subsystems.intake_deploy.commands.IntakeDeployExtendCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

public class DriveToNeutralZoneAndDeployIntakeCommandGroupFactory {
    private final Provider<DriveToNeutralZoneForIntakeCommand> driveToNeutralZoneForIntakeCommandProvider;
    private final Provider<DriveAcrossMidNeutralZoneCommand> driveAcrossMidNeutralZoneCommandProvider;
    private final Provider<IntakeDeployExtendCommand> intakeDeployExtendCommandProvider;
    private final Provider<CollectorIntakeCommand> collectorIntakeCommandProvider;

    @Inject
    public DriveToNeutralZoneAndDeployIntakeCommandGroupFactory(
            Provider<DriveToNeutralZoneForIntakeCommand> driveToNeutralZoneForIntakeCommandProvider,
            Provider<DriveAcrossMidNeutralZoneCommand> driveAcrossMidNeutralZoneCommandProvider,
            Provider<IntakeDeployExtendCommand> intakeDeployExtendCommandProvider,
            Provider<CollectorIntakeCommand> collectorIntakeCommandProvider) {
        this.driveToNeutralZoneForIntakeCommandProvider = driveToNeutralZoneForIntakeCommandProvider;
        this.driveAcrossMidNeutralZoneCommandProvider = driveAcrossMidNeutralZoneCommandProvider;
        this.intakeDeployExtendCommandProvider = intakeDeployExtendCommandProvider;
        this.collectorIntakeCommandProvider = collectorIntakeCommandProvider;
    }

    public SequentialCommandGroup create() {
        var group = new SequentialCommandGroup();
        group.setName("DriveToNeutralZoneAndDeployIntakeCommandGroup");

        group.addCommands(this.driveToNeutralZoneForIntakeCommandProvider.get());
        var driveAcrossAndIntakeDeployCommandGroup = new ParallelCommandGroup(
                this.driveAcrossMidNeutralZoneCommandProvider.get(), intakeDeployExtendCommandProvider.get());

        var driveAcrossIntakeDeployWithFuelIntakeCommand = new ParallelDeadlineGroup(
                driveAcrossAndIntakeDeployCommandGroup, collectorIntakeCommandProvider.get());

        group.addCommands(driveAcrossIntakeDeployWithFuelIntakeCommand);

        return group;
    }

}
