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
    private final Distance robotRadius;
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
        this.robotRadius = electrical_contract.getRadiusOfRobot();
        this.aprilTagFieldLayout = aprilTagFieldLayout;
    }

    private List<XbotSwervePoint> calcSwervePoints() {
        var currentPose = pose.getCurrentPose2d();
        var neutralSideTrenchTag = Landmarks.getClosestTrenchNeutralSideIdPose(this.aprilTagFieldLayout,
                DriverStation.getAlliance().orElse(Alliance.Blue), currentPose);

        // If the edge is above the center then we move along 180 deg otherwise move
        // along 0 deg.
        var multiplier = neutralSideTrenchTag.getY() > this.gamefield.getFieldCenter().getY() ? 1 : -1;
        var adjustedForOffset = new Translation2d(Units.Meters.of(multiplier),
                                                 Units.Meters.of(0));
        var targetPose = new Pose2d(
                neutralSideTrenchTag.getTranslation().plus(adjustedForOffset),
                currentPose.getRotation()
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
