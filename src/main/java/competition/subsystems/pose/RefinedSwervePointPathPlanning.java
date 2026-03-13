package competition.subsystems.pose;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.wpi.first.math.geometry.Pose2d;
import xbot.common.advantage.AKitLogger;
import xbot.common.subsystems.oracle.SwervePointPathPlanning;
import xbot.common.trajectory.XbotSwervePoint;

public class RefinedSwervePointPathPlanning {
    private final SwervePointPathPlanning pathPlanning;
    private final AKitLogger aKitLog;

    private static Logger log = LogManager.getLogger(RefinedSwervePointPathPlanning.class);

    @Inject
    public RefinedSwervePointPathPlanning(SwervePointPathPlanning pathPlanning) {
        this.pathPlanning = pathPlanning;

        this.aKitLog = new AKitLogger("RefinedSwervePointPathPlanning/");
    }

    /**
     * Generates a list of swerve points from starting point while picking the best
     * way through a given path assuming order while avoiding any
     * obstacles in the map.
     *
     * @param startingPose you are at
     * @param path         The set of points that make a path
     * @return a list of swerve points to destination
     */
    public List<XbotSwervePoint> generateSwervePoints(Pose2d startingPose, List<Pose2d> path,
            boolean allowToughTerrain) {
        if (path.size() == 0) {
            return new ArrayList<XbotSwervePoint>();
        }

        List<XbotSwervePoint> swervePoints = new ArrayList<>();
        var pathIndexStart = this.getBestPointOnPath(startingPose, path);
        var trajectoryPoses = new ArrayList<Pose2d>();
        var prefix = String.format("/(%.2f,%.2f)->(%.2f,%.2f)", startingPose.getX(), startingPose.getY(),
                path.get(path.size() - 1).getX(), path.get(path.size() - 1).getY());

        for (int i = pathIndexStart; i < path.size(); i++) {
            var pathPoint = path.get(i);
            trajectoryPoses.add(pathPoint);
            swervePoints.add(new XbotSwervePoint(pathPoint, 2));
        }
        aKitLog.record(prefix + "/trajectory", trajectoryPoses.toArray(new Pose2d[0]));

        return swervePoints;
    }

    private int getBestPointOnPath(Pose2d startingPose, List<Pose2d> path) {
        int bestPointIndex = 0;
        var startingTranslation = startingPose.getTranslation();
        var previousPathPoint = path.get(bestPointIndex);
        var firstPathPointTranslation = previousPathPoint.getTranslation();
        var bestDistance = firstPathPointTranslation.getDistance(startingTranslation);

        for (int i = 1; i < path.size(); i++) {
            var candidate = path.get(i);
            var candidateTranslation = candidate.getTranslation();

            var vectorToCandidate = candidateTranslation.minus(startingTranslation);
            var vectorToPrevious = candidateTranslation.minus(previousPathPoint.getTranslation());
            var distanceToCandidate = candidateTranslation.getDistance(startingTranslation);

            var angleDifference = vectorToCandidate.getAngle().minus(vectorToPrevious.getAngle());
            var degreeDifference = Math.abs(angleDifference.getDegrees());
            log.info("Calc index: {}", i);
            log.info("Degree difference: {}", degreeDifference);
            // If the previous point is in a relative opposite direction of the robot to the
            // candidate then use the new candidate instead.
            if (degreeDifference < 2) {
                bestPointIndex = i;
                bestDistance = distanceToCandidate;
            } else if (distanceToCandidate < bestDistance) {
                bestPointIndex = i;
                bestDistance = distanceToCandidate;
            }
        }

        return bestPointIndex;
    }

}
