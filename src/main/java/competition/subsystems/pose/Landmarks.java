package competition.subsystems.pose;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import xbot.common.subsystems.pose.GameField;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.inject.Singleton;

@Singleton
public class Landmarks {
    // General field
    public static Pose2d overallCenter = new Pose2d(8.270,4.040, Rotation2d.fromDegrees(180));
    public static Pose2d blueOutpostSideTrenchEntrance = new Pose2d(5.540,.650, Rotation2d.fromDegrees(180));
    public static Pose2d blueDepotSideTrenchEntrance = new Pose2d(5.540,7.390, Rotation2d.fromDegrees(180));
    // For exit, refer to blueStartTrenchToOutpost and blueStartTrenchToDepot


    // Starting on blue alliance towards outpost
    public static Pose2d blueStartTrenchToOutpost = new Pose2d(3.57,.650, Rotation2d.fromDegrees(0.00));
    public static Pose2d blueStartBumpToOutpost = new Pose2d(3.57,2.3, Rotation2d.fromDegrees(0));

    // Starting on blue alliance towards depot
    public static Pose2d blueStartTrenchToDepot = new Pose2d(3.57,7.390, Rotation2d.fromDegrees(0));
    public static Pose2d blueStartBumpToDepot = new Pose2d(3.57,5.980, Rotation2d.fromDegrees(0));

    // Starting on blue alliance towards neutral area
    public static Pose2d blueStartBumpToNeutralAreaOutpostSide = new Pose2d(4.480,.65, Rotation2d.fromDegrees(180));
    public static Pose2d blueStartTrenchToNeutralAreaOutpostSide = new Pose2d(4.480,2.6, Rotation2d.fromDegrees(180));

    public static Pose2d blueStartBumpToNeutralAreaDepotSide = new Pose2d(3.580,5.94, Rotation2d.fromDegrees(180));
    public static Pose2d blueStartTrenchToNeutralAreaDepotSide = new Pose2d(4.480,7.390, Rotation2d.fromDegrees(180));

    // Blue Depot
    public static Pose2d blueDepotCollectCenter = new Pose2d(1.180, 5.940, Rotation2d.fromDegrees(180));
    public static Pose2d blueDepotWallSide = new Pose2d(0.460, 6.980, Rotation2d.fromDegrees(270));
    public static Pose2d blueDepotTowerSide = new Pose2d(0.460, 4.950, Rotation2d.fromDegrees(90));

    public static Pose2d blueDepotCenter = new Pose2d(0.270,5.940, Rotation2d.fromDegrees(0));

    // Blue Outpost
    public static Pose2d blueOutpost = new Pose2d(0, 0.650, Rotation2d.fromDegrees(0));
    public static Pose2d blueOutpostPark = new Pose2d(0.350, 0.650, Rotation2d.fromDegrees(0));
    // Blue Tower
    public static Pose2d blueClimbOutpostEdge = new Pose2d(1.510,2.780,Rotation2d.fromDegrees(180));
    public static Pose2d blueClimbMiddleOutpostSide = new Pose2d(1.510,3.120,Rotation2d.fromDegrees(180));
    public static Pose2d blueClimbCenter = new Pose2d(1.510,3.750,Rotation2d.fromDegrees(180));
    public static Pose2d blueClimbMiddleDepotSide = new Pose2d(1.510,4.380,Rotation2d.fromDegrees(180));
    public static Pose2d blueClimbDepotSideEdge = new Pose2d(1.510,4.650,Rotation2d.fromDegrees(180));

    public static Pose2d blueTowerCenter = new Pose2d(.48,3.810, Rotation2d.fromDegrees(0));

    // Field of fuel (For auto)
    public static Pose2d blueOutpostSideFuelFieldCollectionStart = new Pose2d(7.790,1.640, Rotation2d.fromDegrees(90));
    public static Pose2d blueDepotSideFuelFieldCollectionStart = new Pose2d(7.790,6.800, Rotation2d.fromDegrees(270));

    private static Distance halfWidthBallPit = Units.Inches.of(35.95);
    private static Distance halfHeightBallPit = Units.Inches.of(90.95);

    private static Transform2d[] ballPitTransforms = new Transform2d[] {
        new Transform2d(halfWidthBallPit, halfHeightBallPit, Rotation2d.kZero),
        new Transform2d(halfWidthBallPit.times(-1), halfHeightBallPit, Rotation2d.kZero),
        new Transform2d(halfWidthBallPit, halfHeightBallPit.times(-1), Rotation2d.kZero),        
        new Transform2d(halfWidthBallPit.times(-1), halfHeightBallPit.times(-1), Rotation2d.kZero),
    };

    // AprilTag Ids for known points
    // AprilTag IDs
    public static int redCenterHubNeutralSideFiducialId = 4;
    public static int redCenterHubDriverSideFiducialId = 10;

    public static int blueCenterHubNeutralSideFiducialId = 20;
    public static int blueCenterHubDriverSideFiducialId = 26;

    public static int blueTrenchDriverDepotSideId = 23;
    public static int redTrenchDriverDepotSideId = 7;

    public static int blueTrenchNeutralDepotSideId = 22;
    public static int redTrenchNeutralDepotSideId = 6;

    public static int blueTrenchNeutralOutpostSideId = 17;
    public static int redTrenchNeutralOutpostSideId = 1;

    public static int blueTrenchDriverOutpostSideFiducialId = 28;
    public static int redTrenchDriverOutpostSideFiducialId = 12;

    public static int blueOutpostFiducialId = 29;
    public static int redOutpostFiducialId = 13;

    // Offsets/Transforms


    public static List<Integer> getAllianceHubCenterFiducialIds(Alliance alliance) {
        return switch (alliance) {
            case Red -> List.of(redCenterHubNeutralSideFiducialId, redCenterHubDriverSideFiducialId);
            case Blue -> List.of(blueCenterHubNeutralSideFiducialId, blueCenterHubDriverSideFiducialId);
        };
    }

    public static int getAllianceHubNeutralSideFiducialId(Alliance alliance) {
        return alliance == Alliance.Blue
                ? blueCenterHubNeutralSideFiducialId
                : redCenterHubNeutralSideFiducialId;
    }


    public static int getTrenchNeutralDepotSideId(Alliance alliance) {
        return alliance == Alliance.Blue
                ? blueTrenchNeutralDepotSideId
                : redTrenchNeutralDepotSideId;
    }

    public static int getTrenchNeutralOutpostSideId(Alliance alliance) {
        return alliance == Alliance.Blue
                ? blueTrenchNeutralOutpostSideId
                : redTrenchNeutralOutpostSideId;
    }

    public static int getTrenchDriverDepotSideId(Alliance alliance) {
        return alliance == Alliance.Blue
                ? blueTrenchDriverDepotSideId
                : redTrenchDriverDepotSideId;
    }

    public static int getOutpostFiducialId(Alliance alliance) {
        return alliance == Alliance.Blue
                ? blueOutpostFiducialId
                : redOutpostFiducialId;
    }

    public static Pose2d getClosestTrenchNeutralSideIdPose(AprilTagFieldLayout aprilTagFieldLayout, Alliance alliance, Pose2d currentPose) {
        Pose2d trenchNeutralDepotSideIdPose = getAprilTagPose(
                aprilTagFieldLayout,
                getTrenchNeutralDepotSideId(alliance)
        );
        Pose2d trenchNeutralOutpostSideIdPose = getAprilTagPose(
                aprilTagFieldLayout,
                getTrenchNeutralOutpostSideId(alliance)
        );

        double distToDepot = currentPose.getTranslation().getDistance(trenchNeutralDepotSideIdPose.getTranslation());
        double distToOutpost = currentPose.getTranslation().getDistance(trenchNeutralOutpostSideIdPose.getTranslation());

        return distToDepot <= distToOutpost ? trenchNeutralDepotSideIdPose : trenchNeutralOutpostSideIdPose;
    }

    // Get the average pose of the alliance hub using april tags
    public static Pose2d getAllianceHubPose(AprilTagFieldLayout aprilTagFieldLayout, Alliance alliance) {
        var allianceHubCenterTags = Landmarks.getAllianceHubCenterFiducialIds(alliance);

        // Tags to tag poses, to 2d poses
        List<Pose2d> hubCenterTags = getAprilTagPoses(aprilTagFieldLayout, allianceHubCenterTags);

        // Sum across poses to a total X value, same y
        double xTotal = hubCenterTags.stream().map(Pose2d::getX).reduce(0.0, Double::sum);
        double yTotal = hubCenterTags.stream().map(Pose2d::getY).reduce(0.0, Double::sum);

        return new Pose2d(xTotal / hubCenterTags.size(), yTotal / hubCenterTags.size(), Rotation2d.fromDegrees(0));
    }

    public static boolean isBetweenIdX(AprilTagFieldLayout aprilTagFieldLayout, int id1, int id2, Pose2d currentPose) {
        Pose2d id1Pose = getAprilTagPose(aprilTagFieldLayout, id1);
        Pose2d id2Pose = getAprilTagPose(aprilTagFieldLayout, id2);

        double minX = Math.min(id1Pose.getX(), id2Pose.getX());
        double maxX = Math.max(id1Pose.getX(), id2Pose.getX());

        return currentPose.getX() >= minX && currentPose.getX() <= maxX;
    }

    public static Pose2d getClosestAutoBallPitEdge(GameField field, Pose2d currentPose, Alliance alliance) {
        var center = field.getFieldCenter();
        var centerPose = new Pose2d(center, Rotation2d.kZero);
        var ballPitEdges = Arrays.stream(ballPitTransforms)
            .map(transform -> centerPose.transformBy(transform))
            .filter(edgePose -> isInAllianceSide(field, edgePose, alliance))
            .toList();

        var nearestEdge = currentPose.nearest(ballPitEdges);

        if (nearestEdge.getY() > center.getY()) {
            return new Pose2d(nearestEdge.getTranslation(), Rotation2d.kCW_Pi_2);
        } else {
            return new Pose2d(nearestEdge.getTranslation(), Rotation2d.kCCW_Pi_2);
        }
    }

    public static boolean isInAllianceSide(GameField field, Pose2d pose, Alliance alliance) {
        var center = field.getFieldCenter();
        if (alliance == Alliance.Blue) {
            return pose.getX() < center.getX();
        } else if (alliance == Alliance.Red) {
            return pose.getX() > center.getX();
        }

        return false;
    }

    public static List<Pose2d> getAllianceTrenchPoses(AprilTagFieldLayout aprilTagFieldLayout, Alliance alliance) {
        return getAprilTagPoses(aprilTagFieldLayout, getAllianceTrenchFiducialIds(alliance));
    }

    private static List<Integer> getAllianceTrenchFiducialIds(Alliance alliance) {
        return switch (alliance) {
            case Red -> List.of(redTrenchDriverDepotSideId, redTrenchDriverOutpostSideFiducialId);
            case Blue -> List.of(blueTrenchDriverDepotSideId, blueTrenchDriverOutpostSideFiducialId);
        };
    }

    private static List<Pose2d> getAprilTagPoses(AprilTagFieldLayout aprilTagFieldLayout,
            List<Integer> aprilTagFiducialIds) {
        return aprilTagFiducialIds.stream()
            .map(aprilTagFieldLayout::getTagPose)
            .filter(Optional::isPresent)
            .flatMap(Optional::stream)
            .map(Pose3d::toPose2d)
            .toList();
    }

    public static Pose2d getAprilTagPose(AprilTagFieldLayout aprilTagFieldLayout, int aprilTag) {
        return aprilTagFieldLayout
                .getTagPose(aprilTag)
                .orElseThrow(() -> new RuntimeException("AprilTag " + aprilTag + " not found in field layout"))
                .toPose2d();
    }
}
