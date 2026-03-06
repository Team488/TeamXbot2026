
package competition.command_groups;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.Landmarks;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.units.Units;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import xbot.common.injection.electrical_contract.XSwerveDriveElectricalContract;
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
import java.util.ArrayList;
import java.util.List;

import edu.wpi.first.units.measure.Distance;

public class DriveAcrossNeutralZoneCommand extends SwerveSimpleBezierCommand {
    private final PoseSubsystem pose;
    private final SwervePointPathPlanning pathPlanning;
    private final GameField gamefield;
    private final Distance robotRadius;

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
        var x = ballPitEdge.getTranslation().plus(adjustedForRobot).getX();

        var across = new Pose2d(x, gamefield.getFieldCenter().plus(adjustedForRobot).getY(), ballPitEdge.getRotation().minus(Rotation2d.fromDegrees(180)));

        points.add(new XbotSwervePoint(across, 3));

        return points;
    }

    @Override
    public void initialize() {
        super.logic.setKeyPoints(this.calcSwervePoints());

        this.logic.setPrioritizeRotationIfCloseToGoal(true);
        this.logic.setVelocityMode(SwerveSimpleTrajectoryMode.GlobalKinematicsValue);
        super.logic.setGlobalKinematicValues(
                new SwervePointKinematics(1.5, 0.75, 0, 3));

        super.initialize();
    }
}