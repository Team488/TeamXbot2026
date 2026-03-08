package competition.command_groups;

import java.util.List;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.Landmarks;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Distance;
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

public class DriveToOutpostForLoadingCommand extends SwerveSimpleBezierCommand {
    private final DriveSubsystem drive;
    private final PoseSubsystem pose;
    private final SwervePointPathPlanning pathPlanning;
    private final AprilTagFieldLayout aprilTagFieldLayout;

    private static Distance EXTENDED_HOPPER_TO_CENTER = Units.Inches.of(23);

    @Inject
    public DriveToOutpostForLoadingCommand(DriveSubsystem drive, PoseSubsystem pose,
            PropertyFactory pf, HeadingModule.HeadingModuleFactory headingModuleFactory,
            XSwerveDriveElectricalContract electricalContract,
            RobotAssertionManager robotAssertionManager, SwervePointPathPlanning pathPlanning, GameField gamefield,
            Landmarks landmarks, AprilTagFieldLayout aprilTagFieldLayout) {
        super(drive, pose, pf, headingModuleFactory, robotAssertionManager);

        this.drive = drive;
        this.pose = pose;
        this.pathPlanning = pathPlanning;
        this.aprilTagFieldLayout = aprilTagFieldLayout;
    }

    private List<XbotSwervePoint> calcSwervePoints() {
        var outpostTagId = Landmarks.getOutpostFiducialId(DriverStation.getAlliance().orElse(Alliance.Blue));
        var outpostPose = Landmarks.getAprilTagPose(this.aprilTagFieldLayout, outpostTagId);
        var multiplier = DriverStation.getAlliance().orElse(Alliance.Blue) == Alliance.Blue ? 1 : -1;
        var offset = new Transform2d(EXTENDED_HOPPER_TO_CENTER.times(multiplier), Units.Meters.of(0), new Rotation2d(0));
        var endPose = outpostPose.plus(offset);

        var currentPose = pose.getCurrentPose2d();

        return this.pathPlanning.generateSwervePoints(currentPose, endPose,
                false);
    }

    @Override
    public void initialize() {
        var headingPID = this.drive.getRotateToHeadingPid();
        headingPID.setErrorThreshold(5);

        super.logic.setKeyPoints(this.calcSwervePoints());

        this.logic.setPrioritizeRotationIfCloseToGoal(true);
        this.logic.setVelocityMode(SwerveSimpleTrajectoryMode.GlobalKinematicsValue);
        super.logic.setGlobalKinematicValues(
                new SwervePointKinematics(this.drive.getMaxAccelerationMetersPerSecondSquared(), 0, 0,
                        this.drive.getMaxAutoTargetSpeedMetersPerSecond()));

        super.initialize();
    }
}
