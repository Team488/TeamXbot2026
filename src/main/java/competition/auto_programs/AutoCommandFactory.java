package competition.auto_programs;

import competition.command_groups.ContinuousPrepareToShootFromHereCommand;
import competition.command_groups.DriveAcrossMidNeutralZoneCommand;
import competition.command_groups.DriveFromNeutralZoneToAllianceCommand;
import competition.command_groups.DriveToNeutralZoneForIntakeCommand;
import competition.command_groups.DriveToNeutralZoneForIntakeSecondTimeCommand;
import competition.command_groups.DriveToShootingPositionCommand;
import competition.command_groups.FirstDriveForCollectionCommand;
import competition.command_groups.SecondDriveForCollectionCommand;
import competition.command_groups.PrepareToShootCommandGroup;
import competition.subsystems.pose.TrajectoriesCalculation;
import competition.command_groups.RunCollectorHopperFeederCommandGroup;
import competition.command_groups.WaitForRotationAndHoodAndShooterToBeAtGoalCommandGroup;
import competition.general_commands.WaitForDurationCommand;
import competition.subsystems.collector_intake.commands.CollectorIntakeCommand;
import competition.subsystems.collector_intake.commands.CollectorStopCommand;
import competition.subsystems.drive.commands.RotateToHubCommand;
import competition.subsystems.drive.commands.SwerveDriveWithJoysticksCommand;
import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import competition.subsystems.shooter.commands.ShooterStopCommand;
import competition.subsystems.shooter_feeder.commands.ShooterFeederStop;
import competition.subsystems.shooter_feeder.commands.WaitForShootingFinished;
import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import competition.subsystems.intake_deploy.commands.IntakeDeployExtendCommand;
import competition.subsystems.intake_deploy.commands.IntakeDeployOscillating;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.function.Supplier;

public class AutoCommandFactory {

    private final Provider<DriveToNeutralZoneForIntakeCommand> driveToNeutralZoneProvider;
    private final Provider<DriveToNeutralZoneForIntakeSecondTimeCommand> driveToNeutralZoneSecondTimeProvider;
    private final Provider<DriveAcrossMidNeutralZoneCommand> driveAcrossMidNeutralZoneProvider;
    private final Provider<FirstDriveForCollectionCommand> firstDriveForCollectionCommandProvider;
    private final Provider<SecondDriveForCollectionCommand> secondDriveForCollectionCommandProvider;
    private final Provider<IntakeDeployExtendCommand> intakeDeployExtendProvider;
    private final Provider<IntakeDeployOscillating> intakeOscillatingProvider;
    private final Provider<CollectorIntakeCommand> collectorIntakeProvider;
    private final IntakeDeploySubsystem intakeDeploy;
    private final Provider<DriveFromNeutralZoneToAllianceCommand> driveFromNeutralZoneToAllianceProvider;
    private final Provider<CollectorStopCommand> collectorStopProvider;
    private final Provider<DriveToShootingPositionCommand> driveToShootingPositionProvider;
    private final Provider<PrepareToShootCommandGroup> prepareToShootProvider;
    private final Provider<ContinuousPrepareToShootFromHereCommand> continuousPrepareToShootProvider;
    private final Provider<RotateToHubCommand> rotateToHubProvider;
    private final Provider<WaitForRotationAndHoodAndShooterToBeAtGoalCommandGroup> waitForGoalProvider;
    private final Provider<RunCollectorHopperFeederCommandGroup> runFeederProvider;
    private final Provider<ShooterStopCommand> shooterStopProvider;
    private final Provider<ShooterFeederStop> feederStopProvider;
    private final HopperRollerSubsystem hopperRoller;
    private final Provider <WaitForShootingFinished> waitForShootingProvider;
    private final Provider<SwerveDriveWithJoysticksCommand> swerveDriveWithJoysticksProvider;
    private final AutonomousCommandSelector autoSelector;

    @Inject
    public AutoCommandFactory(
            Provider<DriveToNeutralZoneForIntakeCommand> driveToNeutralZoneProvider,
            Provider<DriveToNeutralZoneForIntakeSecondTimeCommand> driveToNeutralZoneSecondTimeProvider,
            Provider<DriveAcrossMidNeutralZoneCommand> driveAcrossMidNeutralZoneProvider,
            Provider<FirstDriveForCollectionCommand> firstDriveForCollectionCommandProvider,
            Provider<SecondDriveForCollectionCommand> secondDriveForCollectionCommandProvider,
            Provider<IntakeDeployExtendCommand> intakeDeployExtendProvider,
            Provider<IntakeDeployOscillating> intakeOscillatingProvider,
            Provider<CollectorIntakeCommand> collectorIntakeProvider,
            IntakeDeploySubsystem intakeDeploy,
            Provider<DriveFromNeutralZoneToAllianceCommand> driveFromNeutralZoneToAllianceProvider,
            Provider<CollectorStopCommand> collectorStopProvider,
            Provider<DriveToShootingPositionCommand> driveToShootingPositionProvider,
            Provider<PrepareToShootCommandGroup> prepareToShootProvider,
            Provider<ContinuousPrepareToShootFromHereCommand> continuousPrepareToShootProvider,
            Provider<RotateToHubCommand> rotateToHubProvider,
            Provider<WaitForRotationAndHoodAndShooterToBeAtGoalCommandGroup> waitForGoalProvider,
            Provider<RunCollectorHopperFeederCommandGroup> runFeederProvider,
            Provider<ShooterStopCommand> shooterStopProvider,
            Provider<ShooterFeederStop> feederStopProvider,
            HopperRollerSubsystem hopperRoller,
            Provider <WaitForShootingFinished> waitForShootingProvider,
            Provider<SwerveDriveWithJoysticksCommand> swerveDriveWithJoysticksProvider,
            AutonomousCommandSelector autoSelector) {
        this.driveToNeutralZoneProvider = driveToNeutralZoneProvider;
        this.driveToNeutralZoneSecondTimeProvider = driveToNeutralZoneSecondTimeProvider;
        this.driveAcrossMidNeutralZoneProvider = driveAcrossMidNeutralZoneProvider;
        this.firstDriveForCollectionCommandProvider = firstDriveForCollectionCommandProvider;
        this.secondDriveForCollectionCommandProvider = secondDriveForCollectionCommandProvider;
        this.intakeDeployExtendProvider = intakeDeployExtendProvider;
        this.intakeOscillatingProvider = intakeOscillatingProvider;
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
        this.shooterStopProvider = shooterStopProvider;
        this.feederStopProvider = feederStopProvider;
        this.hopperRoller = hopperRoller;
        this.autoSelector = autoSelector;
        this.waitForShootingProvider = waitForShootingProvider;
        this.swerveDriveWithJoysticksProvider = swerveDriveWithJoysticksProvider;
    }

    public Command extendIntake() {
        return intakeDeployExtendProvider.get();
    }

    public Command stopShooting() {
        return new InstantCommand().deadlineFor(new ParallelCommandGroup(
                shooterStopProvider.get(),
                feederStopProvider.get(),
                hopperRoller.getStopCommand(),
                collectorStopProvider.get()));
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

        group.addCommands(new ParallelDeadlineGroup(
                firstDriveForCollectionCommandProvider.get(),
                collectorIntakeProvider.get()));

        return group;
    }

        /**
     * Drives to the neutral zone ball pit, deploys the intake, drives across to collect balls.
     */
    public Command collectFromNeutralZoneSecond() {
        var group = new SequentialCommandGroup();
        group.setName("CollectFromNeutralZoneSecond");

        group.addCommands(driveToNeutralZoneSecondTimeProvider.get());

        group.addCommands(new ParallelDeadlineGroup(
                secondDriveForCollectionCommandProvider.get(),
                collectorIntakeProvider.get()));

        return group;
    }

    /**
     * Drives from the neutral zone back to the alliance zone, gets into shooting position,
     * and shoots. The timeout only begins once the shooter and hood are at goal (i.e. it
     * measures how long balls are actively being fed, not the full aim time).
     */
    public Command driveToAllianceAndShoot(Command shootingDeadline) {
        var group = new SequentialCommandGroup();
        group.setName("DriveToAllianceAndShoot");

        var prepareToShoot = prepareToShootProvider.get();
        prepareToShoot.setPresetLocation(TrajectoriesCalculation.PresetShootingDistance.TRENCH);
        group.addCommands(new ParallelDeadlineGroup(
                driveFromNeutralZoneToAllianceProvider.get(),
                collectorStopProvider.get(),
                prepareToShoot));

        var continuousPrepare = continuousPrepareToShootProvider.get();
        continuousPrepare.setTarget(ContinuousPrepareToShootFromHereCommand.ShootingTarget.HUB);
        continuousPrepare.setZeroHood(true);

        var fireAndCloseIntake = runFeederProvider.get().alongWith(intakeOscillatingProvider.get());

        var fireWithTimeout = waitForGoalProvider.get()
                .andThen(shootingDeadline
                        .deadlineFor(fireAndCloseIntake));

        group.addCommands(new ParallelDeadlineGroup(
                fireWithTimeout, // when firing is done, move on
                continuousPrepare,
                swerveDriveWithJoysticksProvider.get(),
                rotateToHubProvider.get()));

        return group;
    }

    /**
     * Drives from the neutral zone back to the alliance zone and shoots with no timeout.
     */
    public Command driveToAllianceAndShootForever() {
        return driveToAllianceAndShoot(new WaitForDurationCommand(() -> Double.MAX_VALUE));
    }

    public Command waitForShootingDone() {
        return waitForShootingProvider.get();
    }
}
