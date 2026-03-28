package competition.subsystems.shooter;

import competition.BaseCompetitionTest;
import competition.subsystems.pose.Landmarks;
import competition.subsystems.pose.TrajectoriesCalculation;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.units.Units;
import edu.wpi.first.wpilibj.DriverStation;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TrajectoriesCalculationTest extends BaseCompetitionTest {

    public Pose2d hub = Landmarks.getAllianceHubPose(
            AprilTagFieldLayout.loadField(AprilTagFields.k2026RebuiltWelded), DriverStation.Alliance.Blue);

    @Test
    public void testTrajectoriesCalculation() {
        TrajectoriesCalculation calc = getInjectorComponent().trajectoriesCalculation();

        // Test that we get valid shooting data back at a known distance
        Pose2d robotPose = new Pose2d(2.0, 0.0, new Rotation2d(0));
        TrajectoriesCalculation.ShootingData data = calc.calculateAllianceHubShootingData(robotPose);

        // Make sure we got something back and not empty/zero data
        assertNotNull(data);
        assertTrue("RPM should be greater than 0", data.shooterRPM().in(Units.RPM) > 0);
    }

    @Test
    public void testInterpolationBetweenTwoPoints() {
        TrajectoriesCalculation calc = getInjectorComponent().trajectoriesCalculation();

        // Use distances in the JSON range (3.797 to 6.105)
        Pose2d closePose = new Pose2d(hub.getX() + 3.8, hub.getY(), new Rotation2d(0));
        Pose2d farPose = new Pose2d(hub.getX() + 6.0, hub.getY(), new Rotation2d(0));
        Pose2d midPose = new Pose2d(hub.getX() + 4.9, hub.getY(), new Rotation2d(0));

        TrajectoriesCalculation.ShootingData closeData = calc.calculateAllianceHubShootingData(closePose);
        TrajectoriesCalculation.ShootingData farData = calc.calculateAllianceHubShootingData(farPose);
        TrajectoriesCalculation.ShootingData midData = calc.calculateAllianceHubShootingData(midPose);

        double closeServo = closeData.servoRatio();
        double farServo = farData.servoRatio();
        double midServo = midData.servoRatio();

        // Servo increases with distance so mid should be between close and far
        assertTrue("Interpolated servo should be between close and far",
                midServo > closeServo && midServo < farServo);
    }

    @Test
    public void testTenRandomPointsInAllianceZone() {
        TrajectoriesCalculation calc = getInjectorComponent().trajectoriesCalculation();

        for (int i = 0; i < 10; i++){

            double randomWidth = (Math.random() * 4.03); // 4.03 is the width of the alliance zone
            double randomHeight = (Math.random() * 8.07); // 8.07 is the height of the alliance zone
            Pose2d randomPose = new Pose2d(randomWidth, randomHeight, new Rotation2d(0)); // Random spot in alliance zone

            TrajectoriesCalculation.ShootingData data = calc.calculateAllianceHubShootingData(randomPose);

            assertNotNull(data);

            if (data.servoRatio() > 0) {
                assertTrue("Servo should be between 0.2 and 1.0",
                        data.servoRatio() >= 0.2 && data.servoRatio() <= 1.0);
            }

        }

    }


}


