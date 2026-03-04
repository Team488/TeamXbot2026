package competition.subsystems.pose;

import edu.wpi.first.math.geometry.Pose2d;
import xbot.common.injection.electrical_contract.XSwerveDriveElectricalContract;
import xbot.common.subsystems.pose.IFieldObstacle;
import xbot.common.subsystems.pose.ObstacleMap;

import java.util.ArrayList;

public class RebuiltObstacleMap extends ObstacleMap {
    public RebuiltObstacleMap(XSwerveDriveElectricalContract electricalContract) {
        // TODO: Build out obstacles
        super(new ArrayList<IFieldObstacle>(), electricalContract);
    }
}
