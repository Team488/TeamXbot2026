package competition.simulation.intake;

import competition.simulation.SimulatorConstants;
import competition.simulation.intake_deploy.IntakeDeploySimulator;
import competition.subsystems.fuel_intake.IntakeSubsystem;
import org.ironmaple.simulation.IntakeSimulation;
import org.ironmaple.simulation.drivesims.AbstractDriveTrainSimulation;
import xbot.common.controls.actuators.mock_adapters.MockCANMotorController;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.Inches;

@Singleton
public class IntakeSimulator {
    final IntakeSubsystem intake;
    final IntakeDeploySimulator intakeDeploySim;
    IntakeSimulation simulation;

    final MockCANMotorController intakeMotor;

    @Inject
    public IntakeSimulator(IntakeSubsystem intake, IntakeDeploySimulator intakeDeploySim) {
        this.intake = intake;
        this.intakeMotor = (MockCANMotorController) intake.intakeMotor;
        this.intakeDeploySim = intakeDeploySim;
    }

    public void initialize(AbstractDriveTrainSimulation driveTrainSimulation) {
        this.simulation = IntakeSimulation.OverTheBumperIntake(
                "Fuel",
                driveTrainSimulation,
                SimulatorConstants.intakeWidth,
                SimulatorConstants.intakeLengthExtended,
                IntakeSimulation.IntakeSide.BACK,
                SimulatorConstants.fuelCapacity
        );
    }

    public boolean isIntaking() {
        return intakeDeploySim.isDeployed() && intakeMotor.getPower() > 0;
    }

    public boolean getPieceFromIntake() {
        return simulation.obtainGamePieceFromIntake();
    }

    public void update() {
        if (isIntaking()) {
            this.simulation.startIntake();
        } else {
            this.simulation.stopIntake();
        }
    }
}
