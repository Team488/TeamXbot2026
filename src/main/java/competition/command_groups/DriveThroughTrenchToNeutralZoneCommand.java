package competition.command_groups;

import java.util.Arrays;
import java.util.List;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.Landmarks;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import xbot.common.injection.electrical_contract.XSwerveDriveElectricalContract;
import xbot.common.logging.RobotAssertionManager;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.SwervePointKinematics;
import xbot.common.subsystems.drive.SwerveSimpleBezierCommand;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryMode;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.subsystems.oracle.SwervePointPathPlanning;
import xbot.common.subsystems.pose.GameField;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;

public class DriveThroughTrenchToNeutralZoneCommand extends SwerveSimpleBezierCommand {
    private final PoseSubsystem pose;
    private final SwervePointPathPlanning pathPlanning;
    private final GameField gamefield;
    private final AprilTagFieldLayout aprilTagFieldLayout;

    @Inject
    public DriveThroughTrenchToNeutralZoneCommand(DriveSubsystem drive, PoseSubsystem pose,
            PropertyFactory pf, HeadingModule.HeadingModuleFactory headingModuleFactory,
            XSwerveDriveElectricalContract electrical_contract,
            RobotAssertionManager robotAssertionManager, SwervePointPathPlanning pathPlanning, GameField gamefield,
            AprilTagFieldLayout aprilTagFieldLayout) {
        super(drive, pose, pf, headingModuleFactory, robotAssertionManager);
        this.pose = pose;
        this.pathPlanning = pathPlanning;
        this.gamefield = gamefield;
        this.aprilTagFieldLayout = aprilTagFieldLayout;
    }

    /* / After some testing, we'll delete this.
    private List<XbotSwervePoint> calcOldSwervePoints() {
        Pose2d closestTrench = this.pose.closestAllianceTrench();
        var fieldCenter = this.gamefield.getFieldCenter();
        var changeInX = DriverStation.getAlliance().orElse(Alliance.Blue) == Alliance.Blue ? -1 : 1;
        var changeInY = closestTrench.getY() > fieldCenter.getY() ? -1 : 1;
        var driverSideTransform = new Transform2d(Units.Meters.of(2 * changeInX), Units.Meters.of(0), Rotation2d.kZero);
        var neutralSideTransform = new Transform2d(Units.Meters.of(2 * -1 * changeInX), Units.Meters.of(0),
                Rotation2d.kZero);
        var finalTransform = new Transform2d(Units.Meters.of(3 * -1 * changeInX), Units.Meters.of(1 * changeInY),
                changeInX * changeInY == 1 ? Rotation2d.kCCW_Pi_2 : Rotation2d.kCW_Pi_2);

        var driverPoint = closestTrench.plus(driverSideTransform);
        var neutralPoint = closestTrench.plus(neutralSideTransform);
        var finalPoint = closestTrench.plus(finalTransform);

        var currentPose = pose.getCurrentPose2d();
        var closestPoint = currentPose.nearest(Arrays.asList(new Pose2d[] { driverPoint, neutralPoint, finalPoint }));

        if (closestPoint == finalPoint) {
            List<XbotSwervePoint> swervePoints = this.pathPlanning.generateSwervePoints(currentPose, finalPoint, false);
            return this.pathPlanning.generateSwervePoints(currentPose, finalPoint, false);
        } else if (closestPoint == neutralPoint) {
            List<XbotSwervePoint> swervePoints = this.pathPlanning.generateSwervePoints(currentPose, neutralPoint,
                    false);
            swervePoints.addAll(this.pathPlanning.generateSwervePoints(neutralPoint, finalPoint, false));

            return swervePoints;
        }

        List<XbotSwervePoint> swervePoints = this.pathPlanning.generateSwervePoints(currentPose, driverPoint,
                false);
        swervePoints.addAll(this.pathPlanning.generateSwervePoints(driverPoint, neutralPoint, false));
        swervePoints.addAll(this.pathPlanning.generateSwervePoints(neutralPoint, finalPoint, false));

        return swervePoints;
    }*/

    private List<XbotSwervePoint> calcSwervePoints() {
        var currentPose = pose.getCurrentPose2d();
        var alliance = DriverStation.getAlliance().orElse(Alliance.Blue);
        var neutralSideTrenchTag = Landmarks.getClosestTrenchNeutralSideIdPose(this.aprilTagFieldLayout,
                alliance, currentPose);

        // If the edge is above the center then we move along 180 deg otherwise move
        // along 0 deg.
        System.out.println(alliance == Alliance.Blue);
        var multiplier = neutralSideTrenchTag.getY() > this.gamefield.getFieldCenter().getY() ? 1 : -1;
        var adjustedForRobot = new Translation2d(
                Units.Meters.of(alliance == Alliance.Blue ? -1.25 : 1.25),
                Units.Meters.of(0)
        );
        var targetPose = new Pose2d(
                neutralSideTrenchTag.getTranslation().plus(adjustedForRobot),
                Rotation2d.fromDegrees(90 * multiplier)
        );

        return this.pathPlanning.generateSwervePoints(currentPose, targetPose,
                false);
    }

    @Override
    public void initialize() {
        super.logic.setKeyPoints(this.calcSwervePoints());

        this.logic.setPrioritizeRotationIfCloseToGoal(true);
        this.logic.setVelocityMode(SwerveSimpleTrajectoryMode.GlobalKinematicsValue);
        super.logic.setGlobalKinematicValues(
                new SwervePointKinematics(2, 1, 0, 4.5));

        super.initialize();
    }
}
