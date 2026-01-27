package competition.simulation.intake;


import competition.subsystems.fuel_intake.IntakeSubsystem;
import edu.wpi.first.units.Units;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Rectangle;
import org.ironmaple.simulation.IntakeSimulation;
import org.ironmaple.simulation.drivesims.AbstractDriveTrainSimulation;

import javax.inject.Inject;
import javax.inject.Singleton;

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
                // How big the intake is
                Inches.of(28),
                Inches.of(12),
                IntakeSimulation.IntakeSide.FRONT,
                30
        );
    }

    // Deployed + intaking motor spinning
    public boolean isIntaking() {
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
