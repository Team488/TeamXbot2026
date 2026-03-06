
package competition.command_groups;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.units.Units;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import xbot.common.logging.RobotAssertionManager;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.SwervePointKinematics;
import xbot.common.subsystems.drive.SwerveSimpleBezierCommand;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryMode;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.subsystems.oracle.SwervePointPathPlanning;
import xbot.common.subsystems.pose.GameField;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import java.util.List;

public class DriveAcrossNeutralZoneCommand extends SwerveSimpleBezierCommand {
    private final PoseSubsystem pose;
    private final SwervePointPathPlanning pathPlanning;
    private final GameField gamefield;

    @Inject
    public DriveAcrossNeutralZoneCommand(DriveSubsystem drive, PoseSubsystem pose,
                                              PropertyFactory pf, HeadingModule.HeadingModuleFactory headingModuleFactory,
                                              XSwerveDriveElectricalContract electricalContract,
                                              RobotAssertionManager robotAssertionManager, SwervePointPathPlanning pathPlanning, GameField gamefield) {
        super(drive, pose, pf, headingModuleFactory, robotAssertionManager);
        this.pose = pose;
        this.pathPlanning = pathPlanning;
        this.gamefield = gamefield;
        this.robotRadius = electricalContract.getRadiusOfRobot();
    }

    private List<XbotSwervePoint> calcSwervePoints() {
        var currentPose = pose.getCurrentPose2d();
        var ballPitEdge = Landmarks.getFurthestAutoBallPitEdge(this.gamefield, currentPose, DriverStation.getAlliance().orElse(Alliance.Blue));

        var multiplier = ballPitEdge.getY() > this.gamefield.getFieldCenter().getY() ? 1 : -1;
        var adjustedForRobot = new Translation2d(Units.Meters.of(0),
                this.robotRadius.times(multiplier));

        List<XbotSwervePoint> points = new ArrayList<>();
        var across = new Pose2d(ballPitEdge.getTranslation().plus(adjustedForRobot), ballPitEdge.getRotation().minus(Rotation2d.fromDegrees(180)));

        points.add(new XbotSwervePoint(across, 3));

        return points;
    }

    @Override
    public void initialize() {
        Pose2d furthestTrench = this.pose.furthestAllianceTrench();
        var fieldCenter = this.gamefield.getFieldCenter();
        var changeInX = DriverStation.getAlliance().orElse(Alliance.Blue) == Alliance.Blue ? -1 : 1;
        var moreCenter = DriverStation.getAlliance().orElse(Alliance.Blue) == Alliance.Blue ? 0.45 : -0.45;
        var changeInY = furthestTrench.getY() > fieldCenter.getY() ? -1 : 1;
        var finalTransform = new Transform2d(Units.Meters.of((3 * -1 * changeInX) + moreCenter), Units.Meters.of(changeInY) ,
                Rotation2d.kZero);

        Pose2d currentPose = pose.getCurrentPose2d();
        var finalTranslation = furthestTrench.plus(finalTransform).getTranslation();
        var finalPoint = new Pose2d(finalTranslation, currentPose.getRotation());
        List<XbotSwervePoint> swervePoints = this.pathPlanning.generateSwervePoints(currentPose, finalPoint, false);
        super.logic.setKeyPoints(swervePoints);

        this.logic.setPrioritizeRotationIfCloseToGoal(true);
        this.logic.setVelocityMode(SwerveSimpleTrajectoryMode.GlobalKinematicsValue);
        super.logic.setGlobalKinematicValues(
                new SwervePointKinematics(1.5, 0.75, 0, 3));

        super.initialize();
    }
}
