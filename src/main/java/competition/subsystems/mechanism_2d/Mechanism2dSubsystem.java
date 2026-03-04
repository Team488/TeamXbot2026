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
    final LoggedMechanismLigament2d hoodLiagment;
    final double intakeDeployLigamentBaseLengthMeters = 165;
    final double hoodBaseLiagamentLengthMeters = 14;


    Angle hoodAngle = Degrees.zero();
    Distance hoodLength = Meters.zero();
    Angle intakeDeployAngle  = Degrees.zero();
    Distance intakeDeployLength = Meters.zero();

    // FakeNumber
    final double intakeDeployAngleDegrees = 0.7;
    final double hoodAngleDegrees = 0.7;

    final Translation2d intakeDeployBasePositionMeters = new Translation2d();
    final Translation2d hoodBasePositionLengthMeters = new Translation2d();


    @Inject
    public Mechanism2dSubsystem(IntakeDeploySubsystem intakeDeploySubsystem, HoodSubsystem hoodSubsystem,
                                LoggedMechanismLigament2d intakeDeployLigament, LoggedMechanismLigament2d hoodLiagment) {
        this.mech2d = new LoggedMechanism2d(1, 3);

        this.intakeDeploySubsystem = intakeDeploySubsystem;
        this.hoodSubsystem = hoodSubsystem;

        this.intakeDeployLigament = new LoggedMechanismLigament2d("intakeDeployMechanism",intakeDeployLigamentBaseLengthMeters,
                intakeDeployAngleDegrees,10,new Color8Bit(Color.kBlue));

        var intakeDeployRoot = mech2d.getRoot("intakeDeployRoot",intakeDeployBasePositionMeters.getX(),intakeDeployBasePositionMeters.getY());
        intakeDeployRoot.append(intakeDeployLigament);

        this.hoodLiagment = new LoggedMechanismLigament2d("hoodMechanism",hoodBaseLiagamentLengthMeters,hoodAngleDegrees, 10,new Color8Bit(Color.kRed));

        var hoodRoot = mech2d.getRoot("hoodRoot", hoodBasePositionLengthMeters.getX(),hoodBasePositionLengthMeters.getY());
        hoodRoot.append(hoodLiagment);
    }
    public void setHoodAngleDegrees(Angle angle){
        hoodAngle = angle;
    }
    public void setIntakeDeployAngleDegrees(Angle angle) {
        intakeDeployAngle = angle;

    }
    public void setIntakeDeployLength(Distance length) {

        intakeDeployLength = length;
    }
    public void setHoodLength(Distance length) {
        hoodLength = length;
    }
    public LoggedMechanism2d getMech2d() {
        intakeDeployLigament.setAngle(intakeDeployAngleDegrees - intakeDeployAngle.in(Degrees));

        intakeDeployLigament.setLength(intakeDeployLigamentBaseLengthMeters + intakeDeployLength.in(Meters));

        hoodLiagment.setAngle(hoodAngleDegrees - hoodAngle.in(Degrees));

        hoodLiagment.setLength(hoodBaseLiagamentLengthMeters - hoodLength.in(Meters));

        return mech2d;
    }
}
