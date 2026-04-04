package competition.auto_programs;

import competition.command_groups.DriveAcrossMidNeutralZoneCommand;
import competition.command_groups.DriveFromNeutralZoneToAllianceCommand;
import competition.command_groups.DriveToNearestShootingPositionAndShootWhenReady;
import competition.command_groups.DriveToNeutralZoneForIntakeCommand;
import competition.command_groups.PrepareToShootCommandGroup;
import competition.subsystems.collector_intake.commands.CollectorIntakeCommand;
import competition.subsystems.collector_intake.commands.CollectorStopCommand;
import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import competition.subsystems.intake_deploy.commands.IntakeDeployExtendCommand;
import competition.subsystems.pose.TrajectoriesCalculation;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.function.Supplier;

public class AutoCommandFactory {

    private final Provider<DriveToNeutralZoneForIntakeCommand> driveToNeutralZoneProvider;
    private final Provider<DriveAcrossMidNeutralZoneCommand> driveAcrossMidNeutralZoneProvider;
    private final Provider<IntakeDeployExtendCommand> intakeDeployExtendProvider;
    private final Provider<CollectorIntakeCommand> collectorIntakeProvider;
    private final IntakeDeploySubsystem intakeDeploy;
    private final Provider<DriveFromNeutralZoneToAllianceCommand> driveFromNeutralZoneToAllianceProvider;
    private final Provider<CollectorStopCommand> collectorStopProvider;
    private final Provider<PrepareToShootCommandGroup> prepareToShootProvider;
    private final DriveToNearestShootingPositionAndShootWhenReady.Factory driveToShootAndShootFactory;
    private final AutonomousCommandSelector autoSelector;

    @Inject
    public AutoCommandFactory(
            Provider<DriveToNeutralZoneForIntakeCommand> driveToNeutralZoneProvider,
            Provider<DriveAcrossMidNeutralZoneCommand> driveAcrossMidNeutralZoneProvider,
            Provider<IntakeDeployExtendCommand> intakeDeployExtendProvider,
            Provider<CollectorIntakeCommand> collectorIntakeProvider,
            IntakeDeploySubsystem intakeDeploy,
            Provider<DriveFromNeutralZoneToAllianceCommand> driveFromNeutralZoneToAllianceProvider,
            Provider<CollectorStopCommand> collectorStopProvider,
            Provider<PrepareToShootCommandGroup> prepareToShootProvider,
            DriveToNearestShootingPositionAndShootWhenReady.Factory driveToShootAndShootFactory,
            AutonomousCommandSelector autoSelector) {
        this.driveToNeutralZoneProvider = driveToNeutralZoneProvider;
        this.driveAcrossMidNeutralZoneProvider = driveAcrossMidNeutralZoneProvider;
        this.intakeDeployExtendProvider = intakeDeployExtendProvider;
        this.collectorIntakeProvider = collectorIntakeProvider;
        this.intakeDeploy = intakeDeploy;
        this.driveFromNeutralZoneToAllianceProvider = driveFromNeutralZoneToAllianceProvider;
        this.collectorStopProvider = collectorStopProvider;
        this.prepareToShootProvider = prepareToShootProvider;
        this.driveToShootAndShootFactory = driveToShootAndShootFactory;
        this.autoSelector = autoSelector;
    }

    public Command statusMessage(String message) {
        return autoSelector.createAutonomousStateMessageCommand(message);
    }

    /**
     * Drives to the neutral zone ball pit, deploys the intake, drives across to collect balls.
     */
    public Command collectFromNeutralZone() {
        var group = new SequentialCommandGroup();
        group.setName("CollectFromNeutralZone");

        group.addCommands(driveToNeutralZoneProvider.get());

        group.addCommands(new SequentialCommandGroup(intakeDeployExtendProvider.get())
                .andThen(intakeDeploy.getWaitForAtGoalCommand()));

        group.addCommands(new ParallelDeadlineGroup(
                driveAcrossMidNeutralZoneProvider.get(),
                collectorIntakeProvider.get()));

        return group;
    }

    /**
     * Drives from the neutral zone back to the alliance zone and shoots, waiting up to
     * {@code timeoutSeconds} for the shot to complete.
     */
    public Command driveToAllianceAndShoot(Supplier<Double> timeoutSeconds) {
        var group = new SequentialCommandGroup();
        group.setName("DriveToAllianceAndShoot");

        var prepareToShoot = prepareToShootProvider.get();
        prepareToShoot.setPresetLocation(TrajectoriesCalculation.PresetShootingDistance.TRENCH);

        group.addCommands(new ParallelDeadlineGroup(
                driveFromNeutralZoneToAllianceProvider.get(),
                collectorStopProvider.get(),
                prepareToShoot));

        group.addCommands(driveToShootAndShootFactory.create(timeoutSeconds));

        return group;
    }

    /**
     * Drives from the neutral zone back to the alliance zone and shoots with no timeout.
     */
    public Command driveToAllianceAndShoot() {
        return driveToAllianceAndShoot(() -> Double.MAX_VALUE);
    }
}
