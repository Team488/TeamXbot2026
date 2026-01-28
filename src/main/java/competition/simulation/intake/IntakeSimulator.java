package competition.simulation.intake;

import competition.subsystems.fuel_intake.IntakeSubsystem;
import org.ironmaple.simulation.IntakeSimulation;
import org.ironmaple.simulation.drivesims.AbstractDriveTrainSimulation;
import xbot.common.controls.actuators.mock_adapters.MockCANMotorController;

import static edu.wpi.first.units.Units.Inches;

public class IntakeSimulator {

    final IntakeSubsystem intake;
    final IntakeSimulation simulation;

    final MockCANMotorController intakeMotor;

    public IntakeSimulator(IntakeSubsystem intake, AbstractDriveTrainSimulation driveTrainSim) {
        this.intake = intake;
        this.intakeMotor = (MockCANMotorController) intake.intakeMotor;
        this.simulation = IntakeSimulation.OverTheBumperIntake(
                "Fuel",
                driveTrainSim,
                Inches.of(28),
                Inches.of(12),
                IntakeSimulation.IntakeSide.FRONT,
                30
        );
    }

    public boolean isIntaking() {
        // TODO: Make sure that we have deployed our intake, too.
        return intakeMotor.getPower() > 0;
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
