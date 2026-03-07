package competition.command_groups;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.fuel_intake.commands.FuelIntakeCommand;
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
    private final Provider<FuelIntakeCommand> fuelIntakeCommandProvider;

    private final DriveSubsystem drive;

    @Inject
    public DriveToNeutralZoneAndDeployIntakeCommandGroupFactory(
            Provider<DriveToNeutralZoneForIntakeCommand> driveToNeutralZoneForIntakeCommandProvider,
            Provider<DriveAcrossMidNeutralZoneCommand> driveAcrossMidNeutralZoneCommandProvider,
            Provider<IntakeDeployExtendCommand> intakeDeployExtendCommandProvider,
            Provider<FuelIntakeCommand> fuelIntakeCommandProvider,
            DriveSubsystem drive) {
        this.driveToNeutralZoneForIntakeCommandProvider = driveToNeutralZoneForIntakeCommandProvider;
        this.driveAcrossMidNeutralZoneCommandProvider = driveAcrossMidNeutralZoneCommandProvider;
        this.intakeDeployExtendCommandProvider = intakeDeployExtendCommandProvider;
        this.fuelIntakeCommandProvider = fuelIntakeCommandProvider;

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
                driveAcrossAndIntakeDeployCommandGroup, fuelIntakeCommandProvider.get());

        group.addCommands(driveAcrossIntakeDeployWithFuelIntakeCommand);

        return group;
    }

}
