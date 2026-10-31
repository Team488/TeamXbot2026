package competition.subsystems.pose;

import edu.wpi.first.math.geometry.Rotation2d;
import xbot.common.controls.sensors.XGyro;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.DegreesPerSecond;

public class XGyroIMU implements XbotIMU {

    private final XGyro gyro;

    public XGyroIMU(XGyro gyro) {
        this.gyro = gyro;
    }

    @Override
    public Rotation2d getRotation() {
        return Rotation2d.fromDegrees(gyro.getHeading().in(Degrees));
    }

    @Override
    public double getAngularVelocity() {
        return gyro.getYawAngularVelocity().in(DegreesPerSecond);
    }

    @Override
    public double getPitch() {
        return gyro.getPitch().in(Degrees);
    }

    @Override
    public double getRoll() {
        return gyro.getRoll().in(Degrees);
    }
}