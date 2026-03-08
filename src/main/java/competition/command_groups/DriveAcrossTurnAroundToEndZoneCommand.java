
package competition.command_groups;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.AutoLandmarks;
import competition.subsystems.pose.PoseSubsystem;
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

public class DriveAcrossTurnAroundToEndZoneCommand extends SwerveSimpleBezierCommand {
    private final AutoLandmarks autoLandmarks;
    private final DriveSubsystem drive;
    private final PoseSubsystem pose;
    private final SwervePointPathPlanning pathPlanning;

    @Inject
    public DriveAcrossTurnAroundToEndZoneCommand(DriveSubsystem drive, PoseSubsystem pose,
            PropertyFactory pf, HeadingModule.HeadingModuleFactory headingModuleFactory,
            RobotAssertionManager robotAssertionManager, SwervePointPathPlanning pathPlanning, GameField gamefield,
            AutoLandmarks autoLandmarks) {
        super(drive, pose, pf, headingModuleFactory, robotAssertionManager);
        this.drive = drive;
        this.pose = pose;
        this.pathPlanning = pathPlanning;
        this.autoLandmarks = autoLandmarks;
    }

    private List<XbotSwervePoint> calcSwervePoints() {
        var currentPose = this.pose.getCurrentPose2d();
        var midPose = this.autoLandmarks.getMidBallPitCollectionPose(currentPose);
        var endPose = this.autoLandmarks.getFinishBallPitCollectionPose(currentPose);

        List<XbotSwervePoint> points = new ArrayList<>();
        points.addAll(this.pathPlanning.generateSwervePoints(midPose, endPose, false));

        return points;
    }

    @Override
    public void initialize() {
        super.logic.setKeyPoints(this.calcSwervePoints());

        this.logic.setPrioritizeRotationIfCloseToGoal(true);
        this.logic.setVelocityMode(SwerveSimpleTrajectoryMode.GlobalKinematicsValue);
        super.logic.setGlobalKinematicValues(
                new SwervePointKinematics(this.drive.getMaxAccelerationMetersPerSecondSquared(), 0, 0,
                        this.drive.getMaxAutoFuelIntakeTargetSpeedMetersPerSecond()));

        super.initialize();
    }
}
