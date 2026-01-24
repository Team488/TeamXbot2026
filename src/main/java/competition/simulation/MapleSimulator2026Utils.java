package competition.simulation;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.units.measure.LinearVelocity;
import org.ironmaple.simulation.seasonspecific.rebuilt2026.RebuiltFuelOnFly;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.MetersPerSecond;
import static org.ironmaple.simulation.seasonspecific.rebuilt2026.Arena2026Rebuilt.randomInRange;

public final class MapleSimulator2026Utils {

    public static RebuiltFuelOnFly newFuelWithVariance(
            Translation2d piecePose,
            Rotation2d yaw,
            Distance height,
            LinearVelocity speed,
            Angle pitch,
            double xVariance,
            double yVariance,
            double yawVariance,
            double speedVariance,
            double pitchVariance) {
        return new RebuiltFuelOnFly(
                piecePose.plus(new Translation2d(randomInRange(xVariance), randomInRange(yVariance))),
                new Translation2d(),
                new ChassisSpeeds(),
                yaw.plus(Rotation2d.fromDegrees(randomInRange(yawVariance))),
                height,
                speed.plus(MetersPerSecond.of(randomInRange(speedVariance))),
                Degrees.of(pitch.in(Degrees) + randomInRange(pitchVariance)));
    }
}
