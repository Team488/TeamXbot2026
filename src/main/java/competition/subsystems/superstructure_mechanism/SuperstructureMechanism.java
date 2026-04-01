package competition.subsystems.superstructure_mechanism;

import competition.subsystems.hood.HoodSubsystem;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj.util.Color8Bit;
import org.littletonrobotics.junction.mechanism.LoggedMechanism2d;
import org.littletonrobotics.junction.mechanism.LoggedMechanismLigament2d;

import static edu.wpi.first.units.Units.Degrees;

public class SuperstructureMechanism {
    final LoggedMechanism2d mech2d;
    final LoggedMechanismLigament2d hoodLigament;
    final LoggedMechanismLigament2d intakeLigament;

    double hoodNormalizedPosition = 0;
    double intakeAngleDegrees = 10;
    double intakeExtendedPositionDegrees = -135;

    // Hood ligament represents the deflector piece at the shooter exit
    final double hoodLigamentLengthMeters = 0.25;

    // Intake ligament represents the deploy arm
    final double intakeLigamentLengthMeters = 0.45;

    public SuperstructureMechanism() {
        this.mech2d = new LoggedMechanism2d(2, 2);

        // Hood: shooter-side of the robot (left side)
        var hoodRoot = mech2d.getRoot("HoodRoot", 0.6, 0.55);
        this.hoodLigament = new LoggedMechanismLigament2d(
                "hood",
                hoodLigamentLengthMeters,
                0,
                5,
                new Color8Bit(Color.kOrange));
        hoodRoot.append(hoodLigament);

        // Intake deploy: pivot is the left end of the arm; at encoder -135 the arm is horizontal-right (0 deg)
        // visual angle = intakeAngle - extendedPositionDegrees (so -135 - (-135) -> 0)
        var intakeRoot = mech2d.getRoot("IntakeRoot", 1.35, 0.25);
        this.intakeLigament = new LoggedMechanismLigament2d(
                "intake",
                intakeLigamentLengthMeters,
                145,
                5,
                new Color8Bit(Color.kGreen));
        intakeRoot.append(intakeLigament);
    }

    public void setHoodNormalizedPosition(double normalized) {
        hoodNormalizedPosition = normalized;
    }

    public void setIntakeAngle(Angle angle) {
        intakeAngleDegrees = angle.in(Degrees);
    }

    public void setIntakeExtendedPosition(double degrees) {
        intakeExtendedPositionDegrees = degrees;
    }

    public LoggedMechanism2d getMechanism() {
        double hoodAngle = hoodNormalizedPosition
                * (HoodSubsystem.mechanismAngleMax - HoodSubsystem.mechanismAngleMin);
        hoodLigament.setAngle(hoodAngle);
        intakeLigament.setAngle(intakeAngleDegrees - intakeExtendedPositionDegrees);
        return mech2d;
    }
}
