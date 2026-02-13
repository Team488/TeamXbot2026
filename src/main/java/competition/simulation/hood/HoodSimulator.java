package competition.simulation.hood;

import competition.subsystems.hood.HoodSubsystem;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.MutAngle;
import edu.wpi.first.wpilibj.MockServo;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.Degrees;

@Singleton
public class HoodSimulator {
    private final MutAngle shootingAngle = Degrees.mutable(80);
//    final TimedServo servo1;
//    final TimedServo servo2;
//
    @Inject
    public HoodSimulator(HoodSubsystem hood) {
//        this.servo1 = hood.hoodServoLeft;
//        this.servo2 = hood.hoodServoRight;
    }

    public Angle getShootingAngle() {
        return shootingAngle.copy();
    }

    public void update() {
        // Update shooting angle
//        double averagePosition = (servo1.get() + servo2.get()) / 2.0;
//        shootingAngle.mut_replace(averagePosition * 100 + 80, Degrees); // TODO: This is arbitrary
    }
}
