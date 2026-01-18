package competition.subsystems.pose;


import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;

import javax.inject.Singleton;

@Singleton
public class Landmarks {
    // Starting on blue alliance towards outpost
    public static Pose2d blueStartTrenchToOutpost = new Pose2d(3.55,.650, Rotation2d.fromDegrees(0));
    public static Pose2d blueStartBumpTpOutpost = new Pose2d(3.55,2.3, Rotation2d.fromDegrees(0));

    //Starting on blue alliance towards depot
    public static Pose2d blueStartTrenchToDepot = new Pose2d(3.55,7.390, Rotation2d.fromDegrees(0));
    public static Pose2d blueStartBumpToDepot = new Pose2d(3.55,5.980, Rotation2d.fromDegrees(0));

    //starting on blue alliance towards neutral area
    public static Pose2d blueStartBumpToNeutralAreaOutpostSide = new Pose2d(4.480,.65, Rotation2d.fromDegrees(180));
    public static Pose2d blueStartTrenchToNeutralAreaOutpostSide = new Pose2d(4.480,2.6, Rotation2d.fromDegrees(180));

    public static Pose2d blueStartBumpToNeutralAreaDepotSide = new Pose2d(4.480,5.94, Rotation2d.fromDegrees(180));
    public static Pose2d blueStartTrenchToNeutralAreaDepotSide = new Pose2d(4.480,7.390, Rotation2d.fromDegrees(180));

    //Starting on red alliance towards outpost
    public static Pose2d redStartTrenchToOutpost = new Pose2d(12.950,7.390, Rotation2d.fromDegrees(0));
    public static Pose2d redStartBumpToOutpost = new Pose2d(12.950,5.980, Rotation2d.fromDegrees(0));

    // Starting on red alliance towards depot
    public static Pose2d redStartTrenchToDepot = new Pose2d(12.950,.650, Rotation2d.fromDegrees(0));
    public static Pose2d redStartBumpToDepot = new Pose2d(12.950,2.3, Rotation2d.fromDegrees(0));

    //starting on red alliance towards neutral area
    public static Pose2d redStartBumpToNeutralAreaOutpostSide = new Pose2d(12.950,5.94, Rotation2d.fromDegrees(180));
    public static Pose2d redStartTrenchToNeutralAreaOutpostSide = new Pose2d(12.080,7.390, Rotation2d.fromDegrees(180));

    public static Pose2d redStartBumpToNeutralAreaDepotSide = new Pose2d(12.080,.65, Rotation2d.fromDegrees(180));
    public static Pose2d redStartTrenchToNeutralAreaDepotSide = new Pose2d(12.950,2.6, Rotation2d.fromDegrees(180));




}
