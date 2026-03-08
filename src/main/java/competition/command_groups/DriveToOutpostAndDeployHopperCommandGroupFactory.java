package competition.command_groups;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.intake_deploy.commands.IntakeDeployExtendCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import javax.inject.Inject;
import javax.inject.Provider;

public class DriveToOutpostAndDeployHopperCommandGroupFactory {
    private final Provider<DriveToOutpostForLoadingCommand> driveToOutpostForLoadingCommandProvider;
    private final Provider<IntakeDeployExtendCommand> intakeDeployExtendCommandProvider;

    @Inject
    public DriveToOutpostAndDeployHopperCommandGroupFactory(
            Provider<DriveToOutpostForLoadingCommand> driveToOutpostForLoadingCommandProvider,
            Provider<IntakeDeployExtendCommand> intakeDeployExtendCommandProvider,
            DriveSubsystem drive) {
        this.driveToOutpostForLoadingCommandProvider = driveToOutpostForLoadingCommandProvider;
        this.intakeDeployExtendCommandProvider = intakeDeployExtendCommandProvider;
    }

    public SequentialCommandGroup create() {
        var group = new SequentialCommandGroup();
        group.setName("DriveToOutpostAndDeployHopperCommandGroupFactory");

        var driveAndDeployCommandGroup = new ParallelCommandGroup(
                this.driveToOutpostForLoadingCommandProvider.get(), intakeDeployExtendCommandProvider.get());

        group.addCommands(driveAndDeployCommandGroup);

        return group;
    }

}
