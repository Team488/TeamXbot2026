package competition.command_groups;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.AutoLandmarks;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import xbot.common.injection.electrical_contract.XSwerveDriveElectricalContract;
import xbot.common.logging.RobotAssertionManager;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.SwervePointKinematics;
import xbot.common.subsystems.drive.SwerveSimpleBezierCommand;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryMode;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.subsystems.oracle.SwervePointPathPlanning;
import xbot.common.subsystems.pose.GameField;

import javax.inject.Inject;

public class DriveFromNeutralZoneToAllianceCommand extends SwerveSimpleBezierCommand {
    private final DriveSubsystem drive;
    private final PoseSubsystem pose;
    private final SwervePointPathPlanning pathPlanning;
    private final AutoLandmarks autoLandmarks;

    @Inject
    public DriveFromNeutralZoneToAllianceCommand(DriveSubsystem drive, PoseSubsystem pose,
            PropertyFactory pf, HeadingModule.HeadingModuleFactory headingModuleFactory,
            XSwerveDriveElectricalContract electricalContract,
            RobotAssertionManager robotAssertionManager, SwervePointPathPlanning pathPlanning, GameField gamefield,
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
        var endPose = this.autoLandmarks.getAllianceShootingStartingPose(currentPose);

        super.logic.setKeyPoints(this.pathPlanning.generateSwervePoints(startPose, endPose,
                false));

        this.logic.setPrioritizeRotationIfCloseToGoal(true);
        this.logic.setVelocityMode(SwerveSimpleTrajectoryMode.GlobalKinematicsValue);
        super.logic.setGlobalKinematicValues(
                new SwervePointKinematics(this.drive.getMaxAccelerationMetersPerSecondSquared(), 0, 0,
                        this.drive.getMaxAutoTargetSpeedMetersPerSecond()));

        super.initialize();
    }
}
