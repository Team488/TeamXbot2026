package competition.command_groups;

import javax.inject.Inject;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.AutoLandmarks;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.pose.RefinedSwervePointPathPlanning;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import xbot.common.injection.electrical_contract.XSwerveDriveElectricalContract;
import xbot.common.logging.RobotAssertionManager;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.SwervePointKinematics;
import xbot.common.subsystems.drive.SwerveSimpleBezierCommand;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryMode;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.subsystems.pose.GameField;

public class DriveFromNeutralZoneToAllianceCommand extends SwerveSimpleBezierCommand {
    private final DriveSubsystem drive;
    private final PoseSubsystem pose;
    private final RefinedSwervePointPathPlanning pathPlanning;
    private final AutoLandmarks autoLandmarks;

    @Inject
    public DriveFromNeutralZoneToAllianceCommand(DriveSubsystem drive, PoseSubsystem pose,
            PropertyFactory pf, HeadingModule.HeadingModuleFactory headingModuleFactory,
            XSwerveDriveElectricalContract electricalContract,
            RobotAssertionManager robotAssertionManager, RefinedSwervePointPathPlanning pathPlanning,
            GameField gamefield,
            AprilTagFieldLayout aprilTagFieldLayout,
            AutoLandmarks autoLandmarks) {
        super(drive, pose, pf, headingModuleFactory, robotAssertionManager);
        this.drive = drive;
        this.pose = pose;
        this.pathPlanning = pathPlanning;
        this.autoLandmarks = autoLandmarks;
    }

    @Override
    public void initialize() {
        var currentPose = this.pose.getCurrentPose2d();
        var startPose = this.autoLandmarks.getFinishBallPitCollectionPose(currentPose);
        var pathPoses = this.autoLandmarks.getAllianceShootingStartingPath(currentPose);

        super.logic.setKeyPoints(this.pathPlanning.generateSwervePoints(startPose, pathPoses,
                false));

        this.logic.setPrioritizeRotationIfCloseToGoal(true);
        this.logic.setVelocityMode(SwerveSimpleTrajectoryMode.GlobalKinematicsValue);
        super.logic.setGlobalKinematicValues(
                new SwervePointKinematics(this.drive.getMaxAccelerationMetersPerSecondSquared(), 0, 0,
                        this.drive.getMaxAutoTargetSpeedMetersPerSecond()));

        super.initialize();
    }
}
