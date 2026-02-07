package competition.simulation;

import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.Distance;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.Inches;
import static edu.wpi.first.units.Units.Meters;

public class SimulatorConstants {

    /** Shooter **/
    public static final Distance shooterHeight = Inches.of(20); // TODO: Tune
    public static final Distance flywheelRadius = Meters.of(0.025); // TODO: Tune
    public static final double positionVariance = 0.50;
    public static final double flywheelJKgMetersSquared = 0.0025;
    public static final double flywheelGearing = 1;

    /** Intake Deploy **/
    public static final Angle intakeDeployedAngle = Degrees.of(80);
    public static final double intakeDeployJKgMetersSquared = 0.005;
    public static final double intakeDeployGearing = 60;
}
