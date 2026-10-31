package competition.subsystems.pose;

import edu.wpi.first.math.geometry.Rotation2d;

public class SystemCoreIMU implements XbotIMU {

    @Override
    public Rotation2d getRotation() {
        return Rotation2d.fromDegrees(0);
    }

    @Override
    public double getAngularVelocity() {
        return 0;
    }

    @Override
    public double getPitch() {
        return 0;
    }

    @Override
    public double getRoll() {
        return 0;
    }
}