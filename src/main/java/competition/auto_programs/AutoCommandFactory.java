package competition.auto_programs;

import competition.command_groups.ContinuousPrepareToShootFromHereCommand;
import competition.command_groups.DriveAcrossMidNeutralZoneCommand;
import competition.command_groups.DriveFromNeutralZoneToAllianceCommand;
import competition.command_groups.DriveToNeutralZoneForIntakeCommand;
import competition.command_groups.DriveToShootingPositionCommand;
import competition.command_groups.PrepareToShootCommandGroup;
import competition.subsystems.pose.TrajectoriesCalculation;
import competition.command_groups.RunCollectorHopperFeederCommandGroup;
import competition.command_groups.WaitForHoodAndShooterToBeAtGoalCommandGroup;
import competition.general_commands.WaitForDurationCommand;
import competition.subsystems.collector_intake.commands.CollectorIntakeCommand;
import competition.subsystems.collector_intake.commands.CollectorStopCommand;
import competition.subsystems.drive.commands.RotateToHubCommand;
import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import competition.subsystems.intake_deploy.commands.IntakeDeployExtendCommand;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
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
    private final Provider<DriveToShootingPositionCommand> driveToShootingPositionProvider;
    private final Provider<PrepareToShootCommandGroup> prepareToShootProvider;
    private final Provider<ContinuousPrepareToShootFromHereCommand> continuousPrepareToShootProvider;
    private final Provider<RotateToHubCommand> rotateToHubProvider;
    private final Provider<WaitForHoodAndShooterToBeAtGoalCommandGroup> waitForGoalProvider;
    private final Provider<RunCollectorHopperFeederCommandGroup> runFeederProvider;
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
            Provider<DriveToShootingPositionCommand> driveToShootingPositionProvider,
            Provider<PrepareToShootCommandGroup> prepareToShootProvider,
            Provider<ContinuousPrepareToShootFromHereCommand> continuousPrepareToShootProvider,
            Provider<RotateToHubCommand> rotateToHubProvider,
            Provider<WaitForHoodAndShooterToBeAtGoalCommandGroup> waitForGoalProvider,
            Provider<RunCollectorHopperFeederCommandGroup> runFeederProvider,
            AutonomousCommandSelector autoSelector) {
        this.driveToNeutralZoneProvider = driveToNeutralZoneProvider;
        this.driveAcrossMidNeutralZoneProvider = driveAcrossMidNeutralZoneProvider;
        this.intakeDeployExtendProvider = intakeDeployExtendProvider;
        this.collectorIntakeProvider = collectorIntakeProvider;
        this.intakeDeploy = intakeDeploy;
        this.driveFromNeutralZoneToAllianceProvider = driveFromNeutralZoneToAllianceProvider;
        this.collectorStopProvider = collectorStopProvider;
        this.driveToShootingPositionProvider = driveToShootingPositionProvider;
        this.prepareToShootProvider = prepareToShootProvider;
        this.continuousPrepareToShootProvider = continuousPrepareToShootProvider;
        this.rotateToHubProvider = rotateToHubProvider;
        this.waitForGoalProvider = waitForGoalProvider;
        this.runFeederProvider = runFeederProvider;
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
     * Drives from the neutral zone back to the alliance zone, gets into shooting position,
     * and shoots. The timeout only begins once the shooter and hood are at goal (i.e. it
     * measures how long balls are actively being fed, not the full aim time).
     */
    public Command driveToAllianceAndShoot(Supplier<Double> shootingTimeoutSeconds) {
        var group = new SequentialCommandGroup();
        group.setName("DriveToAllianceAndShoot");

        group.addCommands(new ParallelDeadlineGroup(
                driveFromNeutralZoneToAllianceProvider.get(),
                collectorStopProvider.get()));

        var prepareToShoot = prepareToShootProvider.get();
        prepareToShoot.setPresetLocation(TrajectoriesCalculation.PresetShootingDistance.TRENCH);
        group.addCommands(new ParallelDeadlineGroup(
                driveToShootingPositionProvider.get(),
                prepareToShoot));

        var continuousPrepare = continuousPrepareToShootProvider.get();
        continuousPrepare.setTarget(ContinuousPrepareToShootFromHereCommand.ShootingTarget.HUB);
        continuousPrepare.setZeroHood(true);

        var fireWithTimeout = waitForGoalProvider.get()
                .andThen(new WaitForDurationCommand(shootingTimeoutSeconds)
                        .deadlineFor(runFeederProvider.get()));

        group.addCommands(new ParallelDeadlineGroup(
                fireWithTimeout, // when firing is done, move on
                continuousPrepare,
                rotateToHubProvider.get()));

        return group;
    }

    /**
     * Drives from the neutral zone back to the alliance zone and shoots with no timeout.
     */
    public Command driveToAllianceAndShoot() {
        return driveToAllianceAndShoot(() -> Double.MAX_VALUE);
    }
}
