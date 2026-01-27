package competition.simulation.intake;


import competition.subsystems.fuel_intake.IntakeSubsystem;
import org.ironmaple.simulation.IntakeSimulation;
import org.ironmaple.simulation.drivesims.AbstractDriveTrainSimulation;

import static edu.wpi.first.units.Units.Inches;

public class IntakeSimulator {

    final IntakeSubsystem intake;
    public final IntakeSimulation simulation;

    // Useful methods
    // obtainGamePieceFromIntake

    public IntakeSimulator(IntakeSubsystem intake, AbstractDriveTrainSimulation driveTrainSim) {
        this.intake = intake;
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
        // Deployed + intaking motor spinning
        return true;
    }

    public void update() {
        if (isIntaking()) {
            this.simulation.startIntake();
        } else {
            this.simulation.stopIntake();
        }
    }
}
