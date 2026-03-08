package competition.subsystems.pose;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import xbot.common.subsystems.oracle.SwervePointPathPlanning;
import xbot.common.subsystems.pose.GameField;

import javax.inject.Inject;
import javax.inject.Singleton;

import competition.electrical_contract.ElectricalContract;

@Singleton
public class AutoLandmarks {
    private final GameField gamefield;
    private final Distance robotRadius;
    private final AprilTagFieldLayout aprilTagFieldLayout;
    private final SwervePointPathPlanning pathPlanning;
    private static final Distance OPTIMAL_DISTANCE_TO_SHOOT_FROM = Units.Inches.of(90);

    @Inject
    public AutoLandmarks(ElectricalContract electricalContract,
            SwervePointPathPlanning pathPlanning,
            AprilTagFieldLayout aprilTagFieldLayout,
            GameField gamefield) {
        this.robotRadius = electricalContract.getRadiusOfRobot();
        this.pathPlanning = pathPlanning;
        this.aprilTagFieldLayout = aprilTagFieldLayout;
        this.gamefield = gamefield;
    }

    public Pose2d getStartCollectionPose(Pose2d pose) {
        var ballPitEdge = Landmarks.getClosestAutoBallPitEdge(this.gamefield, pose,
                DriverStation.getAlliance().orElse(Alliance.Blue));

        // If the edge is above the center then we move along 180 deg otherwise move
        // along 0 deg.
        var multiplierY = ballPitEdge.getY() > this.gamefield.getFieldCenter().getY() ? 1 : -1;
        var multiplierX = this.gamefield.getFieldCenter().getX() > ballPitEdge.getX() ? 1 : -1;
        var adjustedForRobot = new Translation2d(Units.Meters.of(0.5).times(multiplierX),
                this.robotRadius.plus(this.pathPlanning.getAdditionalClearance()).times(multiplierY));

        return new Pose2d(ballPitEdge.getTranslation().plus(adjustedForRobot), ballPitEdge.getRotation());
    }

    public Pose2d getMidBallPitCollectionPose(Pose2d pose) {
        var alliance = DriverStation.getAlliance().orElse(Alliance.Blue);
        var ballPitEdge = Landmarks.getClosestAutoBallPitEdge(this.gamefield, pose, alliance);

        var multiplierY = ballPitEdge.getY() > this.gamefield.getFieldCenter().getY() ? 1 : -1;
        var multiplierX = ballPitEdge.getX() > this.gamefield.getFieldCenter().getX() ? 1 : -1;
        var adjustedForRobot = new Translation2d(Units.Meters.of(0.5).times(multiplierX),
                this.robotRadius.plus(this.pathPlanning.getAdditionalClearance()).times(multiplierY));

        var adjustedTranslation = new Translation2d(ballPitEdge.getX(), this.gamefield.getFieldCenter().getY()).plus(adjustedForRobot);
        return new Pose2d(adjustedTranslation, ballPitEdge.getRotation());
    }

    public Pose2d getFinishBallPitCollectionPose(Pose2d pose) {
        var alliance = DriverStation.getAlliance().orElse(Alliance.Blue);
        var startPoseCollection = this.getStartCollectionPose(pose);
        var nearestAllianceTrenchPose = pose
                .nearest(Landmarks.getAllianceTrenchPoses(this.aprilTagFieldLayout, alliance));
        var multiplier = startPoseCollection.getX() > this.gamefield.getFieldCenter().getX() ? 1 : -1;

        return new Pose2d(startPoseCollection.getX() + multiplier, nearestAllianceTrenchPose.getY(),
                startPoseCollection.getRotation().plus(Rotation2d.kPi));
    }

    public Pose2d getAllianceShootingStartingPose(Pose2d pose) {
        var alliance = DriverStation.getAlliance().orElse(Alliance.Blue);
        var nearestAllianceTrenchPose = pose
                .nearest(Landmarks.getAllianceTrenchPoses(this.aprilTagFieldLayout, alliance));

        var multiplier = nearestAllianceTrenchPose.getX() > this.gamefield.getFieldCenter().getX() ? 1 : -1;
        var adjustedForOffset = new Translation2d(Units.Meters.of(2).times(multiplier),
                Units.Meters.of(0));

        return new Pose2d(nearestAllianceTrenchPose.getTranslation().plus(adjustedForOffset),
                adjustedForOffset.getAngle());
    }

    public Pose2d getClosestShootingPose(Pose2d pose) {
        var alliance = DriverStation.getAlliance().orElse(Alliance.Blue);
        var hubPosition = Landmarks.getAllianceHubPose(this.aprilTagFieldLayout, alliance);

        Translation2d vectorToRobot = pose.getTranslation().minus(hubPosition.getTranslation());
        Rotation2d vectorToRobotAngle = vectorToRobot.getNorm() > 0.01
                ? vectorToRobot.getAngle()
                : Rotation2d.kZero;

        var shootingLocation = hubPosition.getTranslation()
                .plus(new Translation2d(OPTIMAL_DISTANCE_TO_SHOOT_FROM.in(Units.Meters), vectorToRobotAngle));

        return new Pose2d(shootingLocation, vectorToRobot.getAngle());
    }
}
