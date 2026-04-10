package competition.simulation.intake;

import competition.simulation.SimulatorConstants;
import competition.simulation.intake_deploy.IntakeDeploySimulator;
import competition.subsystems.collector_intake.CollectorSubsystem;
import edu.wpi.first.wpilibj.Timer;
import org.ironmaple.simulation.IntakeSimulation;
import org.ironmaple.simulation.drivesims.AbstractDriveTrainSimulation;
import xbot.common.advantage.AKitLogger;
import xbot.common.controls.actuators.mock_adapters.MockCANMotorController;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CollectorSimulator {
    final CollectorSubsystem collector;
    final IntakeDeploySimulator intakeDeploySim;
    IntakeSimulation simulation;

    final MockCANMotorController collectorMotor;

    private int heldPieces = 8;
    final AKitLogger aKitLog = new AKitLogger("Simulator/Collector/");
    final Random random = new Random();

    private static final double COLLECT_CHANCE = 0.4;
    private static final double MAX_COLLECTIONS_PER_SECOND = 8.0;
    private static final double SLIDING_WINDOW_SECONDS = 1.0;
    private final Deque<Double> collectionTimestamps = new ArrayDeque<>();

    @Inject
    public CollectorSimulator(CollectorSubsystem collect, IntakeDeploySimulator intakeDeploySim) {
        this.collector = collect;
        this.collectorMotor = (MockCANMotorController) collect.collectorMotor;
        this.intakeDeploySim = intakeDeploySim;
    }

    public void initialize(AbstractDriveTrainSimulation driveTrainSimulation) {
        this.simulation = IntakeSimulation.OverTheBumperIntake(
                "Fuel",
                driveTrainSimulation,
                SimulatorConstants.collectorWidth,
                SimulatorConstants.collectorLengthExtended,
                IntakeSimulation.IntakeSide.FRONT,
                SimulatorConstants.fuelCapacity
        );

        // Reject balls if over the max collection rate, otherwise collect with 40% probability.
        // Rejected balls stay on the field and bounce off the robot via dyn4j physics.
        this.simulation.setCustomIntakeCondition(gp -> {
            double now = Timer.getFPGATimestamp();
            // Expire old timestamps outside the sliding window
            while (!collectionTimestamps.isEmpty()
                    && collectionTimestamps.peekFirst() < now - SLIDING_WINDOW_SECONDS) {
                collectionTimestamps.pollFirst();
            }
            if (collectionTimestamps.size() >= MAX_COLLECTIONS_PER_SECOND) {
                return false;
            }
            if (random.nextDouble() < COLLECT_CHANCE) {
                collectionTimestamps.addLast(now);
                return true;
            }
            return false;
        });
    }

    public boolean isIntaking() {
        boolean motorActive = collectorMotor.getPower() > 0
                || collectorMotor.getRawTargetVelocity().magnitude() > 0;
        return intakeDeploySim.isDeployed() && motorActive;
    }

    public boolean getPieceFromCollector() {
        if (heldPieces > 0) {
            heldPieces--;
            return true;
        }
        return false;
    }

    public void reset() {
        heldPieces = 8;
    }

    public void update() {
        aKitLog.record("IntakeDeployDeployed", intakeDeploySim.isDeployed());
        aKitLog.record("CollectorMotorPower", collectorMotor.getPower());
        aKitLog.record("IsIntaking", isIntaking());
        aKitLog.record("HeldPieces", heldPieces);

        if (isIntaking()) {
            this.simulation.startIntake();
            if (this.simulation.obtainGamePieceFromIntake()) {
                heldPieces++;
            }
        } else {
            this.simulation.stopIntake();
        }
    }
}
