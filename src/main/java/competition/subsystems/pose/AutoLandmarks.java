package competition.subsystems.pose;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import competition.electrical_contract.ElectricalContract;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.hal.AllianceStationID;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import xbot.common.command.BaseRobot;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.oracle.SwervePointPathPlanning;
import xbot.common.subsystems.pose.GameField;

@Singleton
public class AutoLandmarks {
    private static final Logger logger = LogManager.getLogger(AutoLandmarks.class);

    private final GameField gamefield;
    private final Distance robotRadius;
    private final AprilTagFieldLayout aprilTagFieldLayout;
    private final SwervePointPathPlanning pathPlanning;
    private final DoubleProperty trenchPlanningOffsetMeters;
    private static final Distance OPTIMAL_DISTANCE_TO_SHOOT_FROM = Units.Meters.of(2.74 + 0.34);

    @Inject
    public AutoLandmarks(ElectricalContract electricalContract,
            SwervePointPathPlanning pathPlanning,
            AprilTagFieldLayout aprilTagFieldLayout,
            GameField gamefield, PropertyFactory pf) {
        this.robotRadius = electricalContract.getRadiusOfRobot();
        this.pathPlanning = pathPlanning;
        this.aprilTagFieldLayout = aprilTagFieldLayout;
        this.gamefield = gamefield;

        pf.setPrefix("AutoLandmarks");
        this.trenchPlanningOffsetMeters = pf.createPersistentProperty("trenchPlanningOffsetMeters", 1.5);
    }

    public List<Pose2d> getStartCollectionPath(Pose2d pose) {
        var alliance = this.getAlliance();
        var results = new ArrayList<Pose2d>();

        var paths = this.getNearestAllianceToNeutralTrenchPath(pose);
        results.addAll(paths);
        var ballPitEdge = Landmarks.getClosestAutoBallPitEdge(this.gamefield, pose, alliance);

        // If the edge is above the center then we move along 180 deg otherwise move
        // along 0 deg.
        var multiplierY = ballPitEdge.getY() > this.gamefield.getFieldCenter().getY() ? 1 : -1;
        var multiplierX = this.gamefield.getFieldCenter().getX() > ballPitEdge.getX() ? 1 : -1;
        var adjustedForRobot = new Translation2d(Units.Meters.of(0).times(multiplierX),
                this.robotRadius.plus(this.pathPlanning.getAdditionalClearance()).times(multiplierY));

        var endPosition = new Pose2d(ballPitEdge.getTranslation().plus(adjustedForRobot), ballPitEdge.getRotation());
        results.add(new Pose2d(paths.get(paths.size() - 1).getTranslation().interpolate(endPosition.getTranslation(), 0.2) , ballPitEdge.getRotation()));

        results.add(endPosition);

        return results;
    }

    public Pose2d getStartCollectionPose(Pose2d pose) {
        var path = this.getStartCollectionPath(pose);
        return path.get(path.size() - 1);
    }

    public List<Pose2d> getMidBallPitCollectionPath(Pose2d pose) {
        var results = new ArrayList<Pose2d>();
        var alliance = this.getAlliance();
        var ballPitEdge = Landmarks.getClosestAutoBallPitEdge(this.gamefield, pose, alliance);

        var multiplierY = ballPitEdge.getY() > this.gamefield.getFieldCenter().getY() ? 1 : -1;
        var multiplierX = ballPitEdge.getX() > this.gamefield.getFieldCenter().getX() ? 1 : -1;
        var rotationToDriver = ballPitEdge.getY() > this.gamefield.getFieldCenter().getY() ? Rotation2d.kCCW_Pi_2 : Rotation2d.kCW_Pi_2;
        var adjustedForRobot = new Translation2d(Units.Meters.of(0.5).times(multiplierX),
                this.robotRadius.plus(this.pathPlanning.getAdditionalClearance()).times(multiplierY));

        var adjustedTranslation = new Translation2d(this.gamefield.getFieldCenter().getX(), this.gamefield.getFieldCenter().getY())
                .plus(adjustedForRobot);
        results.add(new Pose2d(adjustedTranslation, ballPitEdge.getRotation()));

        var rotationTowardsDriverStation = alliance == Alliance.Blue
                ? Rotation2d.kZero
                : Rotation2d.kPi;
        var nextCollectionLocation = new Pose2d(
                adjustedTranslation.getX() + (0.5 * multiplierX),
                adjustedTranslation.getY() + (0.75 * multiplierY),
                ballPitEdge.getRotation().plus(rotationToDriver));

        results.add(nextCollectionLocation);

        nextCollectionLocation = new Pose2d(
                nextCollectionLocation.getX(),
                adjustedTranslation.getY() + (0.75 * multiplierY),
                rotationTowardsDriverStation);

        results.add(nextCollectionLocation);

        nextCollectionLocation = new Pose2d(
                nextCollectionLocation.getX() + (0.5 * multiplierX),
                adjustedTranslation.getY() + (0.75 * multiplierY),
                ballPitEdge.getRotation().plus(Rotation2d.kPi));

        results.add(nextCollectionLocation);
        return results;
    }

    public Pose2d getMidBallPitCollectionPose(Pose2d pose) {
        var path = this.getMidBallPitCollectionPath(pose);
        return path.get(path.size() - 1);
    }

    public Pose2d getFinishBallPitCollectionPose(Pose2d pose) {
        var alliance = this.getAlliance();
        var startPoseCollection = this.getStartCollectionPose(pose);
        var nearestAllianceTrenchPose = pose
                .nearest(Landmarks.getAllianceTrenchPoses(this.aprilTagFieldLayout, alliance));
        var multiplierX = startPoseCollection.getX() > this.gamefield.getFieldCenter().getX() ? 1 : -1;
        var multiplierY = this.gamefield.getFieldCenter().getY() > nearestAllianceTrenchPose.getY() ? 1 : -1;
        var returnPathPose = this.getNearestNeutralToAllianceTrenchPath(pose).get(0);

        return new Pose2d(startPoseCollection.getX() + multiplierX,
                nearestAllianceTrenchPose.getY() + (0.125 * multiplierY),
                returnPathPose.getRotation());
    }

    public List<Pose2d> getDriverCloserCollectionPath(Pose2d pose) {
        var results = new ArrayList<Pose2d>();
        var lastDriveFinish = this.getNearestAllianceToNeutralTrenchPath(pose);
        var originalCollectionPose = this.getStartCollectionPose(pose);
        var startPoseCollection = lastDriveFinish.get(lastDriveFinish.size() - 1);

        var multiplierX = this.gamefield.getFieldCenter().getX() > startPoseCollection.getX() ? 1 : -1;
        var multiplierY = this.gamefield.getFieldCenter().getY() > startPoseCollection.getY() ? 1 : -1;
        var firstPoseAdjustment = new Translation2d(Units.Meters.of(0.05).times(multiplierX), Units.Meters.of(0.3).times(multiplierY));
        var firstPathPose = new Pose2d(startPoseCollection.getTranslation().plus(firstPoseAdjustment), originalCollectionPose.getRotation());

        results.add(firstPathPose);

        var adjustedForRobotY = this.robotRadius.plus(this.pathPlanning.getAdditionalClearance()).times(multiplierY * -1).in(Units.Meters);
        var midFieldAdjustedRobotTranslation = new Translation2d(firstPathPose.getX(), this.gamefield.getFieldCenter().getY() + adjustedForRobotY);
        var endCollectionPose = new Pose2d(midFieldAdjustedRobotTranslation, firstPathPose.getRotation());
        results.add(endCollectionPose);

        return results;
    }

    public List<Pose2d> getAllianceShootingStartingPath(Pose2d pose) {
        var alliance = this.getAlliance();
        var results = new ArrayList<Pose2d>();

        var nearestAllianceTrenchPose = pose
                .nearest(Landmarks.getAllianceTrenchPoses(this.aprilTagFieldLayout, alliance));
        var path = this.getNearestNeutralToAllianceTrenchPath(pose);
        results.addAll(path);

        var multiplierX = alliance == Alliance.Blue ? -1 : 1;
        var multiplierY = this.gamefield.getFieldCenter().getY() > nearestAllianceTrenchPose.getY() ? 1 : -1;

        var endAdjustment = new Translation2d(Units.Meters.of(0.05).times(multiplierX), Units.Meters.of(1).times(multiplierY));
        var endTranslation = path.get(path.size() - 1).getTranslation().plus(endAdjustment);
        var hubPosition = Landmarks.getAllianceHubPose(this.aprilTagFieldLayout, alliance);
        Translation2d vectorToHub = endTranslation.minus(hubPosition.getTranslation());
        results.add(new Pose2d(endTranslation,
                vectorToHub.getAngle()));

        return results;
    }

    public Pose2d getAllianceShootingStartingPose(Pose2d pose) {
        var path = this.getAllianceShootingStartingPath(pose);
        return path.get(path.size() - 1);
    }

    public Pose2d getClosestShootingPose(Pose2d pose) {
        var alliance = this.getAlliance();
        var hubPosition = Landmarks.getAllianceHubPose(this.aprilTagFieldLayout, alliance);

        Translation2d vectorToRobot = pose.getTranslation().minus(hubPosition.getTranslation());
        Rotation2d vectorToRobotAngle = vectorToRobot.getNorm() > 0.01
                ? vectorToRobot.getAngle()
                : Rotation2d.kZero;

        var shootingLocation = hubPosition.getTranslation()
                .plus(new Translation2d(OPTIMAL_DISTANCE_TO_SHOOT_FROM.in(Units.Meters), vectorToRobotAngle));

        return new Pose2d(shootingLocation, vectorToRobot.getAngle());
    }

    public List<Pose2d> getNearestAllianceToNeutralTrenchPath(Pose2d startingPose) {
        var alliance = this.getAlliance();
        var results = new ArrayList<Pose2d>();
        var nearestAllianceTrenchPose = startingPose
                .nearest(Landmarks.getAllianceTrenchPoses(this.aprilTagFieldLayout, alliance));

        var changeInX = alliance == Alliance.Blue ? -1 : 1;
        var driverSideTranslation = nearestAllianceTrenchPose.getTranslation()
                .plus(new Translation2d(this.trenchPlanningOffsetMeters.get() * changeInX, 0));
        var neutralSideTranslation = nearestAllianceTrenchPose.getTranslation()
                .plus(new Translation2d(this.trenchPlanningOffsetMeters.get() * -1 * changeInX, 0));
        var rotationThroughTrench = alliance == Alliance.Blue
                ? Rotation2d.kZero
                : Rotation2d.kPi;

        results.add(new Pose2d(driverSideTranslation, rotationThroughTrench));
        results.add(new Pose2d(neutralSideTranslation, rotationThroughTrench));

        return results;
    }

    public List<Pose2d> getNearestNeutralToAllianceTrenchPath(Pose2d startingPose) {
        var path = this.getNearestAllianceToNeutralTrenchPath(startingPose);
        return this.reversePath(path);
    }

    public List<Pose2d> getRelevantAlliancePathThroughTrench(Pose2d startingPose, boolean withLeastAmountOfTurning) {
        var path = this.getNearestAllianceToNeutralTrenchPath(startingPose);
        var leastAmountOfRotation = closestHorizontalRotation(startingPose);
        if (withLeastAmountOfTurning) {
            path = setRotationOnPath(path, leastAmountOfRotation);
        }

        if (path.size() < 2) {
            return path;
        }

        var options = Arrays.asList(path.get(0), path.get(path.size() - 1));
        var nearest = startingPose.nearest(options);
        if (nearest != options.get(0)) {
            if (withLeastAmountOfTurning) {
                return setRotationOnPath(this.reversePath(path), leastAmountOfRotation);
            }
            return this.reversePath(path);
        }

        return path;
    }

    private static List<Pose2d> setRotationOnPath(List<Pose2d> path, Rotation2d rotation) {
        return path.stream()
                .map(pose -> new Pose2d(pose.getTranslation(), rotation))
                .collect(Collectors.toList());
    }

    private static Rotation2d closestHorizontalRotation(Pose2d robotPose) {
        var robotRotation = robotPose.getRotation();
        var deltaToZero = Math.abs(robotRotation.relativeTo(Rotation2d.kZero).getDegrees());
        var deltaToPi = Math.abs(robotRotation.relativeTo(Rotation2d.kPi).getDegrees());

        logger.info("deltaToZero: {} deltaToPi: {}", deltaToZero, deltaToPi);

        if (deltaToPi < deltaToZero) {
            return Rotation2d.kPi;
        } else {
            return Rotation2d.kZero;
        }
    }

    private List<Pose2d> reversePath(List<Pose2d> path) {
        var result = path.stream()
                .map(pose -> new Pose2d(pose.getTranslation(), pose.getRotation().rotateBy(Rotation2d.kPi)))
                .collect(Collectors.toList());

        Collections.reverse(result);

        return result;
    }

    private DriverStation.Alliance getAlliance() {
        return DriverStation.getAlliance().orElse(Alliance.Blue);
    }
}
