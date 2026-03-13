package competition.subsystems.pose;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import competition.electrical_contract.ElectricalContract;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.oracle.SwervePointPathPlanning;
import xbot.common.subsystems.pose.GameField;

@Singleton
public class AutoLandmarks {
    private final GameField gamefield;
    private final Distance robotRadius;
    private final AprilTagFieldLayout aprilTagFieldLayout;
    private final SwervePointPathPlanning pathPlanning;
    private final DoubleProperty trenchPlanningOffsetMeters;
    private static final Distance OPTIMAL_DISTANCE_TO_SHOOT_FROM = Units.Inches.of(103.5);

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
        this.trenchPlanningOffsetMeters = pf.createPersistentProperty("trenchPlanningOffsetMeters", 2);
    }

    public List<Pose2d> getStartCollectionPath(Pose2d pose) {
        var alliance = this.getAlliance();
        var results = new ArrayList<Pose2d>();
        var nearestAllianceTrenchPose = pose
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

        var ballPitEdge = Landmarks.getClosestAutoBallPitEdge(this.gamefield, pose, alliance);

        // If the edge is above the center then we move along 180 deg otherwise move
        // along 0 deg.
        var multiplierY = ballPitEdge.getY() > this.gamefield.getFieldCenter().getY() ? 1 : -1;
        var multiplierX = this.gamefield.getFieldCenter().getX() > ballPitEdge.getX() ? 1 : -1;
        var adjustedForRobot = new Translation2d(Units.Meters.of(0.5).times(multiplierX),
                this.robotRadius.plus(this.pathPlanning.getAdditionalClearance()).times(multiplierY));

        results.add(new Pose2d(ballPitEdge.getTranslation().plus(adjustedForRobot), ballPitEdge.getRotation()));

        return results;
    }

    public Pose2d getStartCollectionPose(Pose2d pose) {
        var path = this.getStartCollectionPath(pose);
        return path.get(path.size() - 1);
    }

    public Pose2d getMidBallPitCollectionPose(Pose2d pose) {
        var alliance = this.getAlliance();
        var ballPitEdge = Landmarks.getClosestAutoBallPitEdge(this.gamefield, pose, alliance);

        var multiplierY = ballPitEdge.getY() > this.gamefield.getFieldCenter().getY() ? 1 : -1;
        var multiplierX = ballPitEdge.getX() > this.gamefield.getFieldCenter().getX() ? 1 : -1;
        var adjustedForRobot = new Translation2d(Units.Meters.of(0.5).times(multiplierX),
                this.robotRadius.plus(this.pathPlanning.getAdditionalClearance()).times(multiplierY));

        var adjustedTranslation = new Translation2d(ballPitEdge.getX(), this.gamefield.getFieldCenter().getY())
                .plus(adjustedForRobot);
        return new Pose2d(adjustedTranslation, ballPitEdge.getRotation());
    }

    public Pose2d getFinishBallPitCollectionPose(Pose2d pose) {
        var alliance = this.getAlliance();
        var startPoseCollection = this.getStartCollectionPose(pose);
        var nearestAllianceTrenchPose = pose
                .nearest(Landmarks.getAllianceTrenchPoses(this.aprilTagFieldLayout, alliance));
        var multiplierX = startPoseCollection.getX() > this.gamefield.getFieldCenter().getX() ? 1 : -1;
        var multiplierY = this.gamefield.getFieldCenter().getY() > nearestAllianceTrenchPose.getY() ? 1 : -1;

        return new Pose2d(startPoseCollection.getX() + multiplierX,
                nearestAllianceTrenchPose.getY() + (0.125 * multiplierY),
                startPoseCollection.getRotation().plus(Rotation2d.kCW_Pi_2));
    }

    public List<Pose2d> getAllianceShootingStartingPath(Pose2d pose) {
        var alliance = this.getAlliance();
        var results = new ArrayList<Pose2d>();
        var nearestAllianceTrenchPose = pose
                .nearest(Landmarks.getAllianceTrenchPoses(this.aprilTagFieldLayout, alliance));

        var changeInX = alliance == Alliance.Blue ? 1 : -1;
        var neutralSideTranslation = nearestAllianceTrenchPose.getTranslation()
                .plus(new Translation2d(this.trenchPlanningOffsetMeters.get() * -1 * changeInX, 0));
        var driverSideTranslation = nearestAllianceTrenchPose.getTranslation()
                .plus(new Translation2d(this.trenchPlanningOffsetMeters.get() * changeInX, 0));
        var rotationThroughTrench = alliance == Alliance.Blue
                ? Rotation2d.kZero
                : Rotation2d.kPi;

        results.add(new Pose2d(neutralSideTranslation, rotationThroughTrench));
        results.add(new Pose2d(driverSideTranslation, rotationThroughTrench));

        var multiplier = nearestAllianceTrenchPose.getX() > this.gamefield.getFieldCenter().getX() ? 1 : -1;
        var adjustedForOffset = new Translation2d(Units.Meters.of(2).times(multiplier),
                Units.Meters.of(0));

        results.add(new Pose2d(nearestAllianceTrenchPose.getTranslation().plus(adjustedForOffset),
                               adjustedForOffset.getAngle()));

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

    private DriverStation.Alliance getAlliance() {
        return DriverStation.getAlliance().orElse(Alliance.Blue);
    }
}
