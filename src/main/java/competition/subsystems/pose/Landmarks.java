package competition.subsystems.pose;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;

import javax.inject.Singleton;

@Singleton
public class Landmarks {
    // Starting on blue alliance towards outpost
    public static Pose2d blueStartTrenchToOutpost = new Pose2d(3.55,.650, Rotation2d.fromDegrees(0));
    public static Pose2d blueStartBumpTpOutpost = new Pose2d(3.55,2.3, Rotation2d.fromDegrees(0));

    // Starting on blue alliance towards depot
    public static Pose2d blueStartTrenchToDepot = new Pose2d(3.55,7.390, Rotation2d.fromDegrees(0));
    public static Pose2d blueStartBumpToDepot = new Pose2d(3.55,5.980, Rotation2d.fromDegrees(0));

    // Starting on blue alliance towards neutral area
    public static Pose2d blueStartBumpToNeutralAreaOutpostSide = new Pose2d(4.480,.65, Rotation2d.fromDegrees(180));
    public static Pose2d blueStartTrenchToNeutralAreaOutpostSide = new Pose2d(4.480,2.6, Rotation2d.fromDegrees(180));

    public static Pose2d blueStartBumpToNeutralAreaDepotSide = new Pose2d(4.480,5.94, Rotation2d.fromDegrees(180));
    public static Pose2d blueStartTrenchToNeutralAreaDepotSide = new Pose2d(4.480,7.390, Rotation2d.fromDegrees(180));

    // Starting on red alliance towards outpost
    public static Pose2d redStartTrenchToOutpost = new Pose2d(12.950,7.390, Rotation2d.fromDegrees(180));
    public static Pose2d redStartBumpToOutpost = new Pose2d(12.950,5.980, Rotation2d.fromDegrees(180));

    // Starting on red alliance towards depot
    public static Pose2d redStartTrenchToDepot = new Pose2d(12.950,.650, Rotation2d.fromDegrees(180));
    public static Pose2d redStartBumpToDepot = new Pose2d(12.950,2.3, Rotation2d.fromDegrees( 180));

    // Starting on red alliance towards neutral area
    public static Pose2d redStartBumpToNeutralAreaOutpostSide = new Pose2d(12.950,5.94, Rotation2d.fromDegrees(180));
    public static Pose2d redStartTrenchToNeutralAreaOutpostSide = new Pose2d(12.080,7.390, Rotation2d.fromDegrees(180));

    public static Pose2d redStartBumpToNeutralAreaDepotSide = new Pose2d(12.080,.65, Rotation2d.fromDegrees(180));
    public static Pose2d redStartTrenchToNeutralAreaDepotSide = new Pose2d(12.950,2.6, Rotation2d.fromDegrees(180));

    // Blue Depot
    public static Pose2d blueDepotCollectCenter = new Pose2d(1.180, 5.940, Rotation2d.fromDegrees(180));
    public static Pose2d blueDepotWallSide = new Pose2d(0.460, 6.980, Rotation2d.fromDegrees(270));
    public static Pose2d blueDepotTowerSide = new Pose2d(0.460, 4.950, Rotation2d.fromDegrees(90));

    // Red Depot
    public static Pose2d redDepotCollectCenter = new Pose2d(15.370, 2.100, Rotation2d.fromDegrees(0));
    public static Pose2d redDepotWallSide = new Pose2d(16.060,1.120, Rotation2d.fromDegrees(90));
    public static Pose2d redDepotTowerSide = new Pose2d(16.060,3.110,Rotation2d.fromDegrees(270));

    // Blue Outpost
    public static Pose2d blueOutpost = new Pose2d(0.470, 0.650, Rotation2d.fromDegrees(180));

    // Red Outpost
    public static Pose2d redOutpost = new Pose2d(16.080, 7.390, Rotation2d.fromDegrees(180));

    // Blue Tower
    public static Pose2d blueClimbOutpostSideEdge = new Pose2d(1.510,2.780,Rotation2d.fromDegrees(180));
    public static Pose2d BlueClimbMiddleOutpostSide = new Pose2d(1.510,3.120,Rotation2d.fromDegrees(180));
    public static Pose2d BlueClimbCenter = new Pose2d(1.510,3.750,Rotation2d.fromDegrees(180));
    public static Pose2d BlueClimbMiddleDepotSide = new Pose2d(1.510,4.380,Rotation2d.fromDegrees(180));
    public static Pose2d BlueClimbDepotSideEdge = new Pose2d(1.510,4.650,Rotation2d.fromDegrees(180));

    // red Tower
    public static Pose2d redClimbDepotSideEdge = new Pose2d(1.510,2.780,Rotation2d.fromDegrees(0));
    public static Pose2d redClimbMiddleDepotSide = new Pose2d(1.510,3.120,Rotation2d.fromDegrees(0));
    public static Pose2d redClimbCenter = new Pose2d(1.510,3.750,Rotation2d.fromDegrees(0));
    public static Pose2d redClimbMiddleOutpostSide = new Pose2d(1.510,4.380,Rotation2d.fromDegrees(0));
    public static Pose2d redClimbDepotOutpostEdge = new Pose2d(1.510,4.650,Rotation2d.fromDegrees(0));
}