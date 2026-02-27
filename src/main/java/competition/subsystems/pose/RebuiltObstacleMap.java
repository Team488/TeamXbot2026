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
import xbot.common.injection.electrical_contract.XSwerveDriveElectricalContract;
import xbot.common.subsystems.pose.IFieldObstacle;
import xbot.common.subsystems.pose.ObstacleMap;

import java.util.ArrayList;
import java.util.List;

public class RebuiltObstacleMap extends ObstacleMap {
    private static List<IFieldObstacle> buildObstacleMap(AprilTagFieldLayout aprilTagFieldLayout) {
        List<IFieldObstacle> obstacles = new ArrayList<IFieldObstacle>();
        var stapleBump = new BumpObstacle(new Translation2d());
        var stapleBumpGuard = new BumpGuardObstacle(new Translation2d());

        var blueAllianceHubPose = Landmarks.getAllianceHubPose(aprilTagFieldLayout, Alliance.Blue);
        var redAllianceHubPose = Landmarks.getAllianceHubPose(aprilTagFieldLayout, Alliance.Red);

        // Add both red and blue hubs, and use blue as a variable for offsets.
        var hubObstacle = new HubObstacle(blueAllianceHubPose.getTranslation());
        obstacles.add(hubObstacle);
        obstacles.add(new HubObstacle(redAllianceHubPose.getTranslation()));

        var hubOffset = hubObstacle.getHalfHeight();

        // Make a transform to find how to go from the center of the hub to the center
        // of the bump.
        var bumpTranform = new Transform2d(hubOffset.plus(stapleBump.getHalfHeight()), Units.Meters.of(0),
                Rotation2d.kZero);

        // Add all 4 bumps by using red vs blue, and taking the transform and using it along with the inverse.
        obstacles.add(new BumpObstacle(blueAllianceHubPose.plus(bumpTranform).getTranslation()));
        obstacles.add(new BumpObstacle(redAllianceHubPose.plus(bumpTranform).getTranslation()));
        obstacles.add(new BumpObstacle(blueAllianceHubPose.plus(bumpTranform.inverse()).getTranslation()));
        obstacles.add(new BumpObstacle(redAllianceHubPose.plus(bumpTranform.inverse()).getTranslation()));

        var hubAndBumpOffset = hubOffset.plus(stapleBump.getHalfHeight().times(2));
        var bumpGuardTransform = new Transform2d(hubAndBumpOffset.plus(stapleBumpGuard.getHalfHeight()), Units.Meters.of(0),
                Rotation2d.kZero);

        obstacles.add(new BumpGuardObstacle(blueAllianceHubPose.plus(bumpGuardTransform).getTranslation()));
        obstacles.add(new BumpGuardObstacle(redAllianceHubPose.plus(bumpGuardTransform).getTranslation()));
        obstacles.add(new BumpGuardObstacle(blueAllianceHubPose.plus(bumpGuardTransform.inverse()).getTranslation()));
        obstacles.add(new BumpGuardObstacle(redAllianceHubPose.plus(bumpGuardTransform.inverse()).getTranslation()));

        return obstacles;
    }

    public RebuiltObstacleMap(AprilTagFieldLayout aprilTagFieldLayout,
            XSwerveDriveElectricalContract electricalContract) {
        super(buildObstacleMap(aprilTagFieldLayout), electricalContract);
    }
}
