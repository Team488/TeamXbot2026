package competition.simulation.intake;

import competition.simulation.SimulatorConstants;
import competition.simulation.intake_deploy.IntakeDeploySimulator;
import competition.subsystems.collector_intake.CollectorSubsystem;
import org.ironmaple.simulation.IntakeSimulation;
import org.ironmaple.simulation.drivesims.AbstractDriveTrainSimulation;
import xbot.common.controls.actuators.mock_adapters.MockCANMotorController;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CollectorSimulator {
    final CollectorSubsystem collector;
    final IntakeDeploySimulator intakeDeploySim;
    IntakeSimulation simulation;

    final MockCANMotorController collectorMotor;

    private int heldPieces = 0;

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
                IntakeSimulation.IntakeSide.BACK,
                SimulatorConstants.fuelCapacity
        );
    }

    public boolean isIntaking() {
        return intakeDeploySim.isDeployed() && collectorMotor.getPower() > 0;
    }

    public boolean getPieceFromCollector() {
        if (heldPieces > 0) {
            heldPieces--;
            return true;
        }
        return false;
    }

    public void update() {
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
