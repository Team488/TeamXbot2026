package competition.subsystems.pose.obstacles;

import xbot.common.subsystems.pose.SquareFieldObstacle;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.units.Units;

// This is the hub obstacle that cannot be moved through. Using dimensions based on pdf:
// https://firstfrc.blob.core.windows.net/frc2026/FieldAssets/2026-field-dimension-dwgs.pdf The
// values are the same between andy mark and welded, but the center is different.  May add 1" to the
// halfWidth and halfHeight due to there being a +- 2" tolerance here.
public class HubObstacle extends SquareFieldObstacle {
    public HubObstacle(Translation2d center) {
        super(center, Units.Inches.of(23.5), true);
    }
}
