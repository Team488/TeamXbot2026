package competition.command_groups;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import competition.command_groups.vision.BaseDriveWithSimpleBezierCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.AutoLandmarks;
import competition.subsystems.pose.Landmarks;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.pose.TrajectoriesCalculation;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import xbot.common.logging.RobotAssertionManager;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.trajectory.XbotSwervePoint;

public class DriveToShootingPositionCommand extends BaseDriveWithSimpleBezierCommand {
    private final DriveSubsystem drive;
    private final PoseSubsystem pose;
    private final AutoLandmarks autoLandmarks;
    private final AprilTagFieldLayout aprilTagFieldLayout;

    @Inject
    public DriveToShootingPositionCommand(DriveSubsystem drive, PoseSubsystem pose, AprilTagFieldLayout aprilTagFieldLayout,
                                          PropertyFactory pf, HeadingModule.HeadingModuleFactory headingModuleFactory,
                                          RobotAssertionManager robotAssertionManager, AutoLandmarks autoLandmarks) {
        super(drive, pose, pf, headingModuleFactory, robotAssertionManager);
        this.drive = drive;
        this.pose = pose;
        this.autoLandmarks = autoLandmarks;
        this.aprilTagFieldLayout = aprilTagFieldLayout;
    }

    @Override
    public void initialize() {
        var currentPose = this.pose.getCurrentPose2d();
        var startPose = this.autoLandmarks.getAllianceShootingStartingPose(currentPose);
        var endPose = this.autoLandmarks.getClosestShootingPose(startPose);
        var alliance = DriverStation.getAlliance().orElse(Alliance.Blue);
        Translation2d targetTranslation = Landmarks.getAllianceHubPose(this.aprilTagFieldLayout, alliance).getTranslation();
        var headingErrorThresholdRatio = TrajectoriesCalculation.calculateRatioForHeadingErrorThreshold(
                this.pose.getCurrentPose2d().getTranslation(), targetTranslation);
        this.drive.setProportionalHeadingErrorThreshold(headingErrorThresholdRatio);

        this.setMaxSpeed(MaxSpeed.Auto);
        List<XbotSwervePoint> swervePoints = new ArrayList<>();
        swervePoints.add(new XbotSwervePoint(endPose, 0.001));
        this.setPrioritizeRotationIfCloseToGoal(true);
        super.logic.setKeyPoints(swervePoints);

        super.initialize();
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
        this.drive.resetHeadingErrorThreshold();
    }
}
