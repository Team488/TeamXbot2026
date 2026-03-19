package competition.command_groups;

import javax.inject.Inject;
import javax.inject.Provider;

import competition.subsystems.collector_intake.commands.CollectorIntakeCommand;
import competition.subsystems.intake_deploy.commands.IntakeDeployExtendCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.command.DelayViaSupplierCommand;

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

        var intakeDeployThenExtendGroup = new SequentialCommandGroup(intakeDeployExtendCommandProvider.get())
                .andThen(new DelayViaSupplierCommand(() -> 0.5))
                .andThen(collectorIntakeCommandProvider.get());

        var driveAcrossAndIntakeDeployCommandGroup = new ParallelCommandGroup(
                this.driveAcrossMidNeutralZoneCommandProvider.get(), intakeDeployThenExtendGroup);

        group.addCommands(driveAcrossAndIntakeDeployCommandGroup);

        return group;
    }

}
