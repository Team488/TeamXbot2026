package competition.subsystems.mechanism_2d;

import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj.util.Color8Bit;
import org.littletonrobotics.junction.mechanism.LoggedMechanism2d;
import org.littletonrobotics.junction.mechanism.LoggedMechanismLigament2d;
import xbot.common.command.BaseSubsystem;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.Meters;
@Singleton
public class Mechanism2dSubsystem extends BaseSubsystem {
    final LoggedMechanism2d mech2d;
    final IntakeDeploySubsystem intakeDeploySubsystem;
    final HoodSubsystem hoodSubsystem;
    final LoggedMechanismLigament2d intakeDeployLigament;
    final double intakeDeployLigamentBaseLengthMeters = 165;

    Angle intakeDeployAngle  = Degrees.zero();
    Distance intakeDeployLength = Meters.zero();

    // FakeNumber
    final double intakeDeployAngleDegrees = 0.7;

    final Translation2d intakeDeployBasePositionMeter = new Translation2d();



    @Inject
    public Mechanism2dSubsystem(IntakeDeploySubsystem intakeDeploySubsystem, HoodSubsystem hoodSubsystem, LoggedMechanismLigament2d intakeDeployLigament) {
        this.mech2d = new LoggedMechanism2d(1, 3);

        this.intakeDeploySubsystem = intakeDeploySubsystem;
        this.hoodSubsystem = hoodSubsystem;

        this.intakeDeployLigament = new LoggedMechanismLigament2d("intakeDeploy",intakeDeployLigamentBaseLengthMeters,
                intakeDeployAngleDegrees,10,new Color8Bit(Color.kBlue));
        var intakeDeployRoot = mech2d.getRoot("intakeDeployRoot",intakeDeployBasePositionMeter.getX(),intakeDeployBasePositionMeter.getY());
        intakeDeployRoot.append(intakeDeployLigament);
    }
    public void setIntakeDeployAngleDegrees(Angle angle) {
        intakeDeployAngle = angle;

    }
    public void setIntakeDeployLength(Distance length) {

        intakeDeployLength = length;
    }
    public LoggedMechanism2d getMech2d() {
        intakeDeployLigament.setAngle(intakeDeployAngleDegrees - intakeDeployAngle.in(Degrees));

        intakeDeployLigament.setLength(intakeDeployLigamentBaseLengthMeters + intakeDeployLength.in(Meters));

        return mech2d;
    }
}
