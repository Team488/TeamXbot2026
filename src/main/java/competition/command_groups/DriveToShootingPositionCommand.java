package competition.command_groups;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.pose.AutoLandmarks;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.pose.TrajectoriesCalculation;
import xbot.common.logging.RobotAssertionManager;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.SwervePointKinematics;
import xbot.common.subsystems.drive.SwerveSimpleBezierCommand;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryMode;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;

public class DriveToShootingPositionCommand extends SwerveSimpleBezierCommand {
    private final DriveSubsystem drive;
    private final HoodSubsystem hood;
    private final PoseSubsystem pose;
    private final TrajectoriesCalculation trajectoriesCalculation;
    private final AutoLandmarks autoLandmarks;

    @Inject
    public DriveToShootingPositionCommand(DriveSubsystem drive, PoseSubsystem pose,
                                          PropertyFactory pf, HeadingModule.HeadingModuleFactory headingModuleFactory,
                                          RobotAssertionManager robotAssertionManager, HoodSubsystem hood,
                                          TrajectoriesCalculation trajectoriesCalculation, AutoLandmarks autoLandmarks) {
        super(drive, pose, pf, headingModuleFactory, robotAssertionManager);
        this.drive = drive;
        this.pose = pose;
        this.hood = hood;
        this.trajectoriesCalculation = trajectoriesCalculation;
        this.autoLandmarks = autoLandmarks;
    }

    @Override
    public void initialize() {
        var currentPose = this.pose.getCurrentPose2d();
        var startPose = this.autoLandmarks.getAllianceShootingStartingPose(currentPose);
        var endPose = this.autoLandmarks.getClosestShootingPose(startPose);

        hood.setTargetValue(trajectoriesCalculation.calculateAllianceHubShootingData(endPose)
                .servoRatio()
        );

        List<XbotSwervePoint> swervePoints = new ArrayList<>();
        swervePoints.add(new XbotSwervePoint(endPose, 0.001));
        super.logic.setKeyPoints(swervePoints);

        this.logic.setPrioritizeRotationIfCloseToGoal(true);
        this.logic.setVelocityMode(SwerveSimpleTrajectoryMode.GlobalKinematicsValue);
        super.logic.setGlobalKinematicValues(
                new SwervePointKinematics(this.drive.getMaxAccelerationMetersPerSecondSquared(), 0, 0,
                        this.drive.getMaxAutoTargetSpeedMetersPerSecond()));

        super.initialize();
    }
}
