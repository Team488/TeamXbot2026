package competition.subsystems.pose;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;

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
    public static Pose2d blueDepotWallSide = new Pose2d(0.460, 6.980, Rotation2d.fromDegrees(270));
    public static Pose2d blueDepotTowerSide = new Pose2d(0.460, 4.950, Rotation2d.fromDegrees(90));

    // Blue Outpost
    public static Pose2d blueOutpost = new Pose2d(0.470, 0.650, Rotation2d.fromDegrees(180));

    // Blue Tower
    public static Pose2d blueClimbOutpostEdge = new Pose2d(1.510,2.780,Rotation2d.fromDegrees(180));
    public static Pose2d blueClimbMiddleOutpostSide = new Pose2d(1.510,3.120,Rotation2d.fromDegrees(180));
    public static Pose2d blueClimbCenter = new Pose2d(1.510,3.750,Rotation2d.fromDegrees(180));
    public static Pose2d blueClimbMiddleDepotSide = new Pose2d(1.510,4.380,Rotation2d.fromDegrees(180));
    public static Pose2d blueClimbDepotSideEdge = new Pose2d(1.510,4.650,Rotation2d.fromDegrees(180));

    // Blue Hub
    public static Pose2d blueHub = new Pose2d(4.62, 4.040, Rotation2d.fromDegrees(0));
}