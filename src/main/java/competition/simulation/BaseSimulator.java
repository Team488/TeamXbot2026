package competition.simulation;

import edu.wpi.first.math.geometry.Pose2d;
import xbot.common.subsystems.pose.SimulatedPositionSupplier;

public interface BaseSimulator extends SimulatedPositionSupplier {
    public void update();

    public void resetPosition(Pose2d pose);
}
