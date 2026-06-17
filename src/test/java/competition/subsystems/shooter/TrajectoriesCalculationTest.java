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
import xbot.common.subsystems.pose.GameField;

import java.util.Random;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TrajectoriesCalculationTest extends BaseCompetitionTest {

    public Pose2d hub = Landmarks.getAllianceHubPose(
            AprilTagFieldLayout.loadField(AprilTagFields.k2026RebuiltWelded), DriverStation.Alliance.Blue);

    private TrajectoriesCalculation.ShootingData getData(
            TrajectoriesCalculation calc,
            Pose2d pose,
            boolean zeroHood) {

        return calc.calculateAllianceHubShootingData(pose, zeroHood);
    }

    @Test
    public void testZeroHoodVsNormal() {
        TrajectoriesCalculation calc = getInjectorComponent().trajectoriesCalculation();


        Pose2d robotPose = new Pose2d(4.0, 2.0, new Rotation2d());

        var normal = calc.calculateAllianceHubShootingData(robotPose, false);
        var zeroHood = calc.calculateAllianceHubShootingData(robotPose, true);

        // Both should return valid data
        assertNotNull(normal);
        assertNotNull(zeroHood);

        // RPM should exist in both
        assertTrue(normal.shooterRPM().in(Units.RPM) > 0);
        assertTrue(zeroHood.shooterRPM().in(Units.RPM) > 0);

        assertTrue("Zero hood servo should be <= normal servo",
                zeroHood.servoRatio() <= normal.servoRatio());
    }

    @Test
    public void testInterpolationBetweenTwoPoints() {
        TrajectoriesCalculation calc = getInjectorComponent().trajectoriesCalculation();

        Pose2d closePose = new Pose2d(hub.getX() + 3.8, hub.getY(), new Rotation2d());
        Pose2d midPose = new Pose2d(hub.getX() + 4.9, hub.getY(), new Rotation2d());
        Pose2d farPose = new Pose2d(hub.getX() + 6.0, hub.getY(), new Rotation2d());

        for (boolean zeroHood : new boolean[]{false, true}) {

            var closeShot = getData(calc, closePose, zeroHood);
            var midShot = getData(calc, midPose, zeroHood);
            var farShot = getData(calc, farPose, zeroHood);

            assertNotNull(closeShot);
            assertNotNull(midShot);
            assertNotNull(farShot);

            if (!zeroHood) {
                // Normal mode → servo should interpolate
                assertTrue(midShot.servoRatio() > closeShot.servoRatio());
                assertTrue(midShot.servoRatio() < farShot.servoRatio());
            } else {
                // Zero hood → servo should NOT change (likely 0)
                assertTrue(Math.abs(midShot.servoRatio() - closeShot.servoRatio()) < 0.001);
                assertTrue(Math.abs(midShot.servoRatio() - farShot.servoRatio()) < 0.001);
            }
        }
    }

    @Test
    public void testTenRandomPointsInAllianceZone() {
        TrajectoriesCalculation calc = getInjectorComponent().trajectoriesCalculation();

        long seed = System.currentTimeMillis();
        Random rand = new Random(seed);

        System.out.println("Seed: " + seed);

        for (boolean zeroHood : new boolean[]{false, true}) {
            GameField gameField =  getInjectorComponent().gameField();
            for (int i = 0; i < 10; i++) {
                double randomWidth = rand.nextDouble() * gameField.getFieldWidth().in(Units.Meters); // 4.03 is the width of the alliance zone
                double randomLength = rand.nextDouble() * gameField.getFieldLength().in(Units.Meters); // 8.07 is the height of the alliance zone

                Pose2d randomPose = new Pose2d(randomWidth, randomLength, new Rotation2d(0)); // Random spot in alliance zone

                TrajectoriesCalculation.ShootingData data = calc.calculateAllianceHubShootingData(randomPose,zeroHood);

                assertNotNull(data);

                // Servo ratio should be between 0 (No hood) and 1.0 (Max hood)
                if (data.servoRatio() > 0) {
                    assertTrue("Servo should be between 0.0 and 1.0",
                            data.servoRatio() <= 1.0);
                }
            }
        }
    }
}