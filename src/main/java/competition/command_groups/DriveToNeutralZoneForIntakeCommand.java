package competition.command_groups;

import java.util.List;

import javax.inject.Inject;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.AutoLandmarks;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.pose.RefinedSwervePointPathPlanning;
import xbot.common.injection.electrical_contract.XSwerveDriveElectricalContract;
import xbot.common.logging.RobotAssertionManager;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.SwervePointKinematics;
import xbot.common.subsystems.drive.SwerveSimpleBezierCommand;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryMode;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.trajectory.XbotSwervePoint;

public class DriveToNeutralZoneForIntakeCommand extends SwerveSimpleBezierCommand {
    private final DriveSubsystem drive;
    private final PoseSubsystem pose;
    private final RefinedSwervePointPathPlanning pathPlanning;
    private final AutoLandmarks autoLandmarks;

    @Inject
    public DriveToNeutralZoneForIntakeCommand(DriveSubsystem drive, PoseSubsystem pose,
            PropertyFactory pf, HeadingModule.HeadingModuleFactory headingModuleFactory,
            XSwerveDriveElectricalContract electricalContract,
            RobotAssertionManager robotAssertionManager, RefinedSwervePointPathPlanning pathPlanning,
            AutoLandmarks autoLandmarks) {
        super(drive, pose, pf, headingModuleFactory, robotAssertionManager);

        this.drive = drive;
        this.pose = pose;
        this.pathPlanning = pathPlanning;
        this.autoLandmarks = autoLandmarks;
    }

    private List<XbotSwervePoint> calcSwervePoints() {
        var currentPose = pose.getCurrentPose2d();
        var pathPoses = this.autoLandmarks.getStartCollectionPath(currentPose);

        return this.pathPlanning.generateSwervePoints(currentPose, pathPoses,
                false);
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
