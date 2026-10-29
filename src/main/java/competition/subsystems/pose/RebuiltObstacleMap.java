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
    private static RectangleFieldObstacle logObstacle(RectangleFieldObstacle obstacle) {
        var center = obstacle.center();
        var prefix = String.format("RebuiltObstacleMap/Obstacle(%.2f,%.2f)", center.getX(), center.getY());

        Logger.recordOutput(prefix + "/halfHeight", obstacle.getHalfHeight());
        Logger.recordOutput(prefix + "/halfWidth", obstacle.getHalfWidth());

        var translations = new Translation2d[] {
            new Translation2d(obstacle.getHalfWidth(), obstacle.getHalfHeight()),
            new Translation2d(obstacle.getHalfWidth().times(-1), obstacle.getHalfHeight()),
            new Translation2d(obstacle.getHalfWidth(), obstacle.getHalfHeight().times(-1)),
            new Translation2d(obstacle.getHalfWidth().times(-1), obstacle.getHalfHeight().times(-1)),
        };

        var positions = new Translation2d[translations.length];
        for (int i = 0; i <  translations.length; i++) {
            var translation = translations[i];
            positions[i] = center.plus(translation);
        }
        Logger.recordOutput(prefix + "/positions", positions);

        return obstacle;
    }

    private static List<IFieldObstacle> buildObstacleMap(AprilTagFieldLayout aprilTagFieldLayout) {
        AKitLogger aKitLog = new AKitLogger("RebuiltObstacleMap");

        List<IFieldObstacle> obstacles = new ArrayList<IFieldObstacle>();
        var stapleBump = new BumpObstacle(new Translation2d());
        var stapleBumpGuard = new BumpGuardObstacle(new Translation2d());

        var blueAllianceHubPose = Landmarks.getAllianceHubPose(aprilTagFieldLayout, Alliance.Blue);
        var redAllianceHubPose = Landmarks.getAllianceHubPose(aprilTagFieldLayout, Alliance.Red);

        // Add both red and blue hubs, and use blue as a variable for offsets.
        var hubObstacle = logObstacle(new HubObstacle(blueAllianceHubPose.getTranslation()));
        obstacles.add(hubObstacle);
        obstacles.add(logObstacle(new HubObstacle(redAllianceHubPose.getTranslation())));

        var hubOffset = hubObstacle.getHalfHeight();

        // Make a transform to find how to go from the center of the hub to the center
        // of the bump.
        var bumpTranform = new Transform2d(Units.Meters.of(0), hubOffset.plus(stapleBump.getHalfHeight()),
                Rotation2d.kZero);

        // Add all 4 bumps by using red vs blue, and taking the transform and using it along with the inverse.
        obstacles.add(logObstacle(new BumpObstacle(blueAllianceHubPose.plus(bumpTranform).getTranslation())));
        obstacles.add(logObstacle(new BumpObstacle(redAllianceHubPose.plus(bumpTranform).getTranslation())));
        obstacles.add(logObstacle((new BumpObstacle(blueAllianceHubPose.plus(bumpTranform.inverse()).getTranslation()))));
        obstacles.add(logObstacle((new BumpObstacle(redAllianceHubPose.plus(bumpTranform.inverse()).getTranslation()))));

        var hubAndBumpOffset = hubOffset.plus(stapleBump.getHalfHeight().times(2));
        var bumpGuardTransform = new Transform2d(Units.Meters.of(0), hubAndBumpOffset.plus(stapleBumpGuard.getHalfHeight()),
                Rotation2d.kZero);

        obstacles.add(logObstacle(new BumpGuardObstacle(blueAllianceHubPose.plus(bumpGuardTransform).getTranslation())));
        obstacles.add(logObstacle(new BumpGuardObstacle(redAllianceHubPose.plus(bumpGuardTransform).getTranslation())));
        obstacles.add(logObstacle(new BumpGuardObstacle(blueAllianceHubPose.plus(bumpGuardTransform.inverse()).getTranslation())));
        obstacles.add(logObstacle(new BumpGuardObstacle(redAllianceHubPose.plus(bumpGuardTransform.inverse()).getTranslation())));

        return obstacles;
    }

    public RebuiltObstacleMap(AprilTagFieldLayout aprilTagFieldLayout,
            XSwerveDriveElectricalContract electricalContract) {
        super(buildObstacleMap(aprilTagFieldLayout), electricalContract);
    }
}
