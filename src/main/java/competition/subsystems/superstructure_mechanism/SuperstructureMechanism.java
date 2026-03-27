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

        // Hood: shooter-side of the robot (right side)
        var hoodRoot = mech2d.getRoot("HoodRoot", 1.4, 0.55);
        this.hoodLigament = new LoggedMechanismLigament2d(
                "hood",
                hoodLigamentLengthMeters,
                180,
                5,
                new Color8Bit(Color.kOrange));
        hoodRoot.append(hoodLigament);

        // Intake deploy: pivot is the right end of the arm; at encoder -135 the arm is horizontal-left (180 deg)
        // visual angle = encoder angle + 315 (so -135 -> 180)
        var intakeRoot = mech2d.getRoot("IntakeRoot", 0.65, 0.25);
        this.intakeLigament = new LoggedMechanismLigament2d(
                "intake",
                intakeLigamentLengthMeters,
                35,
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
        double hoodAngle = 180 - hoodNormalizedPosition
                * (HoodSubsystem.mechanismAngleMax - HoodSubsystem.mechanismAngleMin);
        hoodLigament.setAngle(hoodAngle);
        intakeLigament.setAngle(180 + intakeExtendedPositionDegrees - intakeAngleDegrees);
        return mech2d;
    }
}
