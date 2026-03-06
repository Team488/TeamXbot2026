package competition.command_groups;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.Landmarks;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import xbot.common.logging.RobotAssertionManager;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.SwervePointKinematics;
import xbot.common.subsystems.drive.SwerveSimpleBezierCommand;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryMode;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.subsystems.oracle.SwervePointPathPlanning;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import java.util.List;

public class DriveToShootingPositionCommand extends SwerveSimpleBezierCommand {
    private final DriveSubsystem drive;
    private final PoseSubsystem pose;
    private final SwervePointPathPlanning pathPlanning;
    private final AprilTagFieldLayout aprilTagFieldLayout;
    private static final Distance OPTIMAL_DISTANCE_TO_SHOOT_FROM = Units.Inches.of(90);

    @Inject
    public DriveToShootingPositionCommand(DriveSubsystem drive, PoseSubsystem pose,
                                          AprilTagFieldLayout aprilTagFieldLayout,
                                          PropertyFactory pf, HeadingModule.HeadingModuleFactory headingModuleFactory,
                                          RobotAssertionManager robotAssertionManager, SwervePointPathPlanning pathPlanning) {
        super(drive, pose, pf, headingModuleFactory, robotAssertionManager);
        this.drive = drive;
        this.pose = pose;
        this.aprilTagFieldLayout = aprilTagFieldLayout;
        this.pathPlanning = pathPlanning;
    }

    private List<XbotSwervePoint> calcSwervePoints() {
        Pose2d currentPose = pose.getCurrentPose2d();

        return this.pathPlanning.generateSwervePoints(currentPose, this.shootingLocationAndRotation(),
                false);
    }

    private Pose2d shootingLocationAndRotation() {
        Pose2d currentPose = pose.getCurrentPose2d();
        Pose2d allianceHubPose = Landmarks.getAllianceHubPose(this.aprilTagFieldLayout, DriverStation.getAlliance().orElse(Alliance.Blue));
        Translation2d translationToHub = allianceHubPose.minus(currentPose).getTranslation();
        Translation2d translationToClosestShootingPoint = new Translation2d(OPTIMAL_DISTANCE_TO_SHOOT_FROM.in(Units.Meters),
                translationToHub.getAngle());

        return new Pose2d(allianceHubPose.getTranslation().plus(translationToClosestShootingPoint), translationToHub.getAngle());
    }

    @Override
    public void initialize() {
        super.logic.setKeyPoints(this.calcSwervePoints());

        this.logic.setPrioritizeRotationIfCloseToGoal(true);
        this.logic.setVelocityMode(SwerveSimpleTrajectoryMode.GlobalKinematicsValue);
        super.logic.setGlobalKinematicValues(
                new SwervePointKinematics(this.drive.getMaxAccelerationMetersPerSecondSquared(), 0, 0,
                        this.drive.getMaxAutoTargetSpeedMetersPerSecond()));

        super.initialize();
    }
}
