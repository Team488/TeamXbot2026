package competition.command_groups;

import java.util.Arrays;
import java.util.List;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.Landmarks;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import xbot.common.logging.RobotAssertionManager;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.SwervePointKinematics;
import xbot.common.subsystems.drive.SwerveSimpleBezierCommand;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryMode;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.subsystems.oracle.SwervePointPathPlanning;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;

public class DriveToNeutralZoneForIntakeCommand extends SwerveSimpleBezierCommand {
    private final PoseSubsystem pose;
    private final SwervePointPathPlanning pathPlanning;

    @Inject
    public DriveToNeutralZoneForIntakeCommand(DriveSubsystem drive, PoseSubsystem pose,
                                              PropertyFactory pf, HeadingModule.HeadingModuleFactory headingModuleFactory,
                                              RobotAssertionManager robotAssertionManager, SwervePointPathPlanning pathPlanning) {
        super(drive, pose, pf, headingModuleFactory, robotAssertionManager);
        this.pose = pose;
        this.pathPlanning = pathPlanning;
    }

    @Override
    public void initialize() {
        Pose2d closestTrench = this.pose.closestAllianceTrench();
        var changeInY = DriverStation.getAlliance().orElse(Alliance.Blue) == Alliance.Blue ? -1 : 1;
        var driverSideTransform = new Transform2d(Units.Meters.of(0), Units.Meters.of(1.5 * changeInY), Rotation2d.kZero);
        var neutralSideTransform = new Transform2d(Units.Meters.of(0), Units.Meters.of(1.5 * -1 * changeInY), Rotation2d.kZero);
        var finalTransform = new Transform2d(Units.Meters.of(1), Units.Meters.of(2 * -1 * changeInY), Rotation2d.kZero);

        var driverPoint = closestTrench.plus(driverSideTransform);
        var neutralPoint = closestTrench.plus(neutralSideTransform);
        var finalPoint = closestTrench.plus(finalTransform);

        var currentPose = pose.getCurrentPose2d();
        var closestPoint = currentPose.nearest(Arrays.asList(new Pose2d[] {driverPoint, neutralPoint, finalPoint}));

        if (closestPoint == finalPoint) {
            List<XbotSwervePoint> swervePoints = this.pathPlanning.generateSwervePoints(currentPose, finalPoint, false);
            super.logic.setKeyPoints(swervePoints);
        } else if (closestPoint == neutralPoint) {
            List<XbotSwervePoint> swervePoints =
            this.pathPlanning.generateSwervePoints(currentPose, neutralPoint, false);
            swervePoints.addAll(this.pathPlanning.generateSwervePoints(neutralPoint, finalPoint, false));

            super.logic.setKeyPoints(swervePoints);
        } else {
            List<XbotSwervePoint> swervePoints =
            this.pathPlanning.generateSwervePoints(currentPose, driverPoint, false);
            swervePoints.addAll(this.pathPlanning.generateSwervePoints(driverPoint, neutralPoint, false));
            swervePoints.addAll(this.pathPlanning.generateSwervePoints(neutralPoint, finalPoint, false));

            super.logic.setKeyPoints(swervePoints);
        }

        this.logic.setPrioritizeRotationIfCloseToGoal(true);
        this.logic.setVelocityMode(SwerveSimpleTrajectoryMode.GlobalKinematicsValue);
        super.logic.setGlobalKinematicValues(
                new SwervePointKinematics(2, 1, 0, 4.5));

        super.initialize();
    }
}
