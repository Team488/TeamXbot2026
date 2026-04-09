package competition.auto_programs.ppl;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.path.GoalEndState;
import com.pathplanner.lib.path.PathConstraints;
import com.pathplanner.lib.path.PathPlannerPath;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.AutoLandmarks;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.units.measure.LinearVelocity;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static edu.wpi.first.units.Units.MetersPerSecond;

public class PathPlanner {

    public static PathConstraints PATH_CONSTRAINTS = new PathConstraints(
            4.5,   // max velocity m/s
            3.0,   // max acceleration m/s^2
            Math.toRadians(540),  // max angular velocity rad/s
            Math.toRadians(720)   // max angular acceleration rad/s^2
    );

    private final DriveSubsystem drive;
    private final PoseSubsystem pose;
    private final AutoLandmarks autoLandmarks;

    @Inject
    public PathPlanner(DriveSubsystem drive, PoseSubsystem pose, AutoLandmarks autoLandmarks) {
        this.drive = drive;
        this.pose = pose;
        this.autoLandmarks = autoLandmarks;
    }

    // TODO: This is long enough, move this into a separate file
    public Command basePathfindingCommand(
            Supplier<List<Pose2d>> waypointsSupplier,
            LinearVelocity endingVelocity,
            Rotation2d endingRotation,
            PathConstraints constraints
    ) {
        return Commands.defer(() -> {
            var poses = waypointsSupplier.get();
            var waypoints = PathPlannerPath.waypointsFromPoses(poses);
            if (poses.isEmpty()) {
                return Commands.none(); // Edge case of no Pose2d
            }

            Pose2d destination = poses.get(poses.size() - 1);
            Rotation2d rotation = endingRotation;
            if (rotation == null) {
                rotation = destination.getRotation(); // Allow for endingRotation = null
            }

            var path = new PathPlannerPath(
                    waypoints,
                    constraints,
                    null,
                    new GoalEndState(endingVelocity.in(MetersPerSecond), rotation)
            );
            path.preventFlipping = true;

            return AutoBuilder.followPath(path)
                    .withName("PathfindingTo " + destination);
        }, Set.of(drive));
    }

    public Command driveToNeutralZoneForIntake() {
        return basePathfindingCommand(
                () -> autoLandmarks.getStartCollectionPath(pose.getCurrentPose2d()),
                MetersPerSecond.zero(),
                Rotation2d.kCW_90deg,
                PATH_CONSTRAINTS
        );
    }

    public Command driveAcrossMidNeutralZone() {
        return basePathfindingCommand(
                () -> {
                    var currentPose = pose.getCurrentPose2d();
                    var startPose = autoLandmarks.getStartCollectionPose(currentPose);
                    var midPose = autoLandmarks.getMidBallPitCollectionPose(currentPose);
                    var endPose = autoLandmarks.getFinishBallPitCollectionPose(currentPose);
                    return List.of(startPose, midPose, endPose);
                },
                MetersPerSecond.zero(),
                Rotation2d.kCW_90deg,
                PATH_CONSTRAINTS
        );
    }

    public Command driveFromNeutralToAlliance() {
        return basePathfindingCommand(
                () -> {
                    var currentPose = pose.getCurrentPose2d();
                    var startPose = autoLandmarks.getFinishBallPitCollectionPose(currentPose);
                    var pathPoses = autoLandmarks.getAllianceShootingStartingPath(currentPose);
                    return List.of(startPose, pathPoses.get(pathPoses.size() - 1));
                },
                MetersPerSecond.zero(),
                null,
                PATH_CONSTRAINTS
        );
    }
}
