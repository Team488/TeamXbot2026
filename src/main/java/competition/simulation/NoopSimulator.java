package competition.simulation;

import javax.inject.Inject;

import edu.wpi.first.math.geometry.Pose2d;

public class NoopSimulator implements BaseSimulator {
    @Inject
    public NoopSimulator() {
        // Do nothing
    }

    public void update() {
        // Do nothing, used just in case a real robot accidentally tries to call this method
    }

    @Override
    public void resetPosition(Pose2d pose) {
        // Do nothing, used just in case a real robot accidentally tries to call this method
    }

    @Override
    public Pose2d getGroundTruthPose() {
        return new Pose2d();
    }
}