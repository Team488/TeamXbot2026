package competition.subsystems.pose;

import edu.wpi.first.math.geometry.Rotation2d;

public interface XbotIMU {
    Rotation2d getRotation();
    double getAngularVelocity();
    double getPitch();
    double getRoll();
}