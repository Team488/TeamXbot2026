package competition.subsystems.pose;

import competition.subsystems.pose.obstacles.HubObstacle;
import competition.subsystems.pose.obstacles.BumpObstacle;
import competition.subsystems.pose.obstacles.BumpGuardObstacle;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import xbot.common.advantage.AKitLogger;
import xbot.common.injection.electrical_contract.XSwerveDriveElectricalContract;
import xbot.common.subsystems.pose.IFieldObstacle;
import xbot.common.subsystems.pose.ObstacleMap;
import xbot.common.subsystems.pose.RectangleFieldObstacle;

import java.util.ArrayList;
import java.util.List;

import org.littletonrobotics.junction.Logger;

public class RebuiltObstacleMap extends ObstacleMap {
    private static void logObstacle(String label, RectangleFieldObstacle obstacle, Distance halfWidth, Distance halfHeight) {
        var center = obstacle.center();
        var prefix = String.format("RebuiltObstacleMap/%s(%.2f,%.2f)", label, center.getX(), center.getY());

        Logger.recordOutput(prefix + "/halfHeight", halfHeight);
        Logger.recordOutput(prefix + "/halfWidth", halfWidth);

        var translations = new Translation2d[] {
            new Translation2d(halfWidth, halfHeight),
            new Translation2d(halfWidth.times(-1), halfHeight),
            new Translation2d(halfWidth, halfHeight.times(-1)),
            new Translation2d(halfWidth.times(-1), halfHeight.times(-1)),
        };

        var positions = new Translation2d[translations.length];
        for (int i = 0; i <  translations.length; i++) {
            var translation = translations[i];
            positions[i] = center.plus(translation);
        }
        Logger.recordOutput(prefix + "/positions", positions);
    }

    private static List<IFieldObstacle> buildObstacleMap(AprilTagFieldLayout aprilTagFieldLayout) {
        AKitLogger aKitLog = new AKitLogger("RebuiltObstacleMap");

        List<IFieldObstacle> obstacles = new ArrayList<IFieldObstacle>();

        var blueAllianceHubPose = Landmarks.getAllianceHubPose(aprilTagFieldLayout, Alliance.Blue);
        var redAllianceHubPose = Landmarks.getAllianceHubPose(aprilTagFieldLayout, Alliance.Red);

        // Add both red and blue hubs, and use blue as a variable for offsets.
        var blueHub = new HubObstacle(blueAllianceHubPose.getTranslation());
        logObstacle("Hub", blueHub, HubObstacle.HALF_SIZE, HubObstacle.HALF_SIZE);
        obstacles.add(blueHub);

        var redHub = new HubObstacle(redAllianceHubPose.getTranslation());
        logObstacle("Hub", redHub, HubObstacle.HALF_SIZE, HubObstacle.HALF_SIZE);
        obstacles.add(redHub);

        var hubOffset = HubObstacle.HALF_SIZE;

        // Make a transform to find how to go from the center of the hub to the center
        // of the bump.
        var bumpTranform = new Transform2d(Units.Meters.of(0), hubOffset.plus(BumpObstacle.HALF_HEIGHT),
                Rotation2d.kZero);

        // Add all 4 bumps by using red vs blue, and taking the transform and using it along with the inverse.
        var blueBump1 = new BumpObstacle(blueAllianceHubPose.plus(bumpTranform).getTranslation());
        logObstacle("Bump", blueBump1, BumpObstacle.HALF_WIDTH, BumpObstacle.HALF_HEIGHT);
        obstacles.add(blueBump1);

        var redBump1 = new BumpObstacle(redAllianceHubPose.plus(bumpTranform).getTranslation());
        logObstacle("Bump", redBump1, BumpObstacle.HALF_WIDTH, BumpObstacle.HALF_HEIGHT);
        obstacles.add(redBump1);

        var blueBump2 = new BumpObstacle(blueAllianceHubPose.plus(bumpTranform.inverse()).getTranslation());
        logObstacle("Bump", blueBump2, BumpObstacle.HALF_WIDTH, BumpObstacle.HALF_HEIGHT);
        obstacles.add(blueBump2);

        var redBump2 = new BumpObstacle(redAllianceHubPose.plus(bumpTranform.inverse()).getTranslation());
        logObstacle("Bump", redBump2, BumpObstacle.HALF_WIDTH, BumpObstacle.HALF_HEIGHT);
        obstacles.add(redBump2);

        var hubAndBumpOffset = hubOffset.plus(BumpObstacle.HALF_HEIGHT.times(2));
        var bumpGuardTransform = new Transform2d(Units.Meters.of(0), hubAndBumpOffset.plus(BumpGuardObstacle.HALF_HEIGHT),
                Rotation2d.kZero);

        var blueGuard1 = new BumpGuardObstacle(blueAllianceHubPose.plus(bumpGuardTransform).getTranslation());
        logObstacle("BumpGuard", blueGuard1, BumpGuardObstacle.HALF_WIDTH, BumpGuardObstacle.HALF_HEIGHT);
        obstacles.add(blueGuard1);

        var redGuard1 = new BumpGuardObstacle(redAllianceHubPose.plus(bumpGuardTransform).getTranslation());
        logObstacle("BumpGuard", redGuard1, BumpGuardObstacle.HALF_WIDTH, BumpGuardObstacle.HALF_HEIGHT);
        obstacles.add(redGuard1);

        var blueGuard2 = new BumpGuardObstacle(blueAllianceHubPose.plus(bumpGuardTransform.inverse()).getTranslation());
        logObstacle("BumpGuard", blueGuard2, BumpGuardObstacle.HALF_WIDTH, BumpGuardObstacle.HALF_HEIGHT);
        obstacles.add(blueGuard2);

        var redGuard2 = new BumpGuardObstacle(redAllianceHubPose.plus(bumpGuardTransform.inverse()).getTranslation());
        logObstacle("BumpGuard", redGuard2, BumpGuardObstacle.HALF_WIDTH, BumpGuardObstacle.HALF_HEIGHT);
        obstacles.add(redGuard2);

        return obstacles;
    }

    public RebuiltObstacleMap(AprilTagFieldLayout aprilTagFieldLayout,
            XSwerveDriveElectricalContract electricalContract) {
        super(buildObstacleMap(aprilTagFieldLayout), electricalContract);
    }
}
