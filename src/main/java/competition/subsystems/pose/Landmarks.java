package competition.subsystems.pose;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.DriverStation.Alliance;

import java.util.List;
import java.util.Optional;
import javax.inject.Singleton;

@Singleton
public class Landmarks {
    // Starting on blue alliance towards outpost
    public static Pose2d blueStartTrenchToOutpost = new Pose2d(3.57,.650, Rotation2d.fromDegrees(0));
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
    public static Pose2d blueDepotWallSide = new Pose2d(0.44, 6.980, Rotation2d.fromDegrees(270));
    public static Pose2d blueDepotTowerSide = new Pose2d(0.44, 4.950, Rotation2d.fromDegrees(270));

    // Blue Outpost
    public static Pose2d blueOutpost = new Pose2d(0.470, 0.650, Rotation2d.fromDegrees(180));

    // Blue Tower
    public static Pose2d blueClimbOutpostEdge = new Pose2d(1.510,2.780,Rotation2d.fromDegrees(180));
    public static Pose2d blueClimbMiddleOutpostSide = new Pose2d(1.510,3.120,Rotation2d.fromDegrees(180));
    public static Pose2d blueClimbCenter = new Pose2d(1.510,3.750,Rotation2d.fromDegrees(180));
    public static Pose2d blueClimbMiddleDepotSide = new Pose2d(1.510,4.380,Rotation2d.fromDegrees(180));
    public static Pose2d blueClimbDepotSideEdge = new Pose2d(1.510,4.650,Rotation2d.fromDegrees(180));

    // Blue Hub
    // TODO: delete this and use getAllianceHubPose instead
    public static Pose2d blueHub = new Pose2d(4.62, 4.040, Rotation2d.fromDegrees(0));

    public static int redCenterHubNeutralSideFiducialId = 4;
    public static int redCenterHubDriverSideFiducialId = 10;

    public static int blueCenterHubNeutralSideFiducialId = 20;
    public static int blueCenterHubDriverSideFiducialId = 26;

    public static int blueTrenchDriverDepotSideFiducialId = 23;
    public static int redTrenchDriverDepotSideFiducialId = 7;

    public static int blueOutpostFiducialId = 29;
    public static int redOutpostFiducialId = 13;

    public static List<Integer> getAllianceHubCenterFiducialIds(Alliance alliance) {
        return switch (alliance) {
            case Red -> List.of(redCenterHubNeutralSideFiducialId, redCenterHubDriverSideFiducialId);
            case Blue -> List.of(blueCenterHubNeutralSideFiducialId, blueCenterHubDriverSideFiducialId);
        };
    }
    public static int getTrenchDriverDepotSideId(Alliance alliance) {
        return alliance == Alliance.Blue
                ? blueTrenchDriverDepotSideFiducialId
                : redTrenchDriverDepotSideFiducialId;
    }

    public static int getOutpostId(Alliance alliance) {
        return alliance == Alliance.Blue
                ? blueOutpostFiducialId
                : redOutpostFiducialId;
    }

    // Get the average pose of the alliance hub using april tags
    public static Pose2d getAllianceHubPose(AprilTagFieldLayout aprilTagFieldLayout, Alliance alliance) {
        var allianceHubCenterTags = Landmarks.getAllianceHubCenterFiducialIds(alliance);

        // Tags to tag poses, to 2d poses
        List<Pose2d> hubCenterTags = allianceHubCenterTags.stream()
                .map(aprilTagFieldLayout::getTagPose)
                .filter(Optional::isPresent)
                .flatMap(Optional::stream)
                .map(Pose3d::toPose2d)
                .toList();

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

    private static Pose2d getAprilTagPose(AprilTagFieldLayout aprilTagFieldLayout, int aprilTag) {
        return aprilTagFieldLayout
                .getTagPose(aprilTag)
                .orElseThrow(() -> new RuntimeException("AprilTag " + aprilTag + " not found in field layout"))
                .toPose2d();
    }
}
