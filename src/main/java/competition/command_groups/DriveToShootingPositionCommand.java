package competition.command_groups;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import competition.command_groups.vision.BaseDriveWithSimpleBezierCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.AutoLandmarks;
import competition.subsystems.pose.PoseSubsystem;
import xbot.common.logging.RobotAssertionManager;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.trajectory.XbotSwervePoint;

public class DriveToShootingPositionCommand extends BaseDriveWithSimpleBezierCommand {
    private final PoseSubsystem pose;
    private final AutoLandmarks autoLandmarks;

    @Inject
    public DriveToShootingPositionCommand(DriveSubsystem drive, PoseSubsystem pose,
                                          PropertyFactory pf, HeadingModule.HeadingModuleFactory headingModuleFactory,
                                          RobotAssertionManager robotAssertionManager, AutoLandmarks autoLandmarks) {
        super(drive, pose, pf, headingModuleFactory, robotAssertionManager);
        this.pose = pose;
        this.autoLandmarks = autoLandmarks;
    }

    @Override
    public void initialize() {
        var currentPose = this.pose.getCurrentPose2d();
        var startPose = this.autoLandmarks.getAllianceShootingStartingPose(currentPose);
        var endPose = this.autoLandmarks.getClosestShootingPose(startPose);

        this.setMaxSpeed(MaxSpeed.Auto);
        List<XbotSwervePoint> swervePoints = new ArrayList<>();
        swervePoints.add(new XbotSwervePoint(endPose, 0.001));
        super.logic.setKeyPoints(swervePoints);

        super.initialize();
    }
}
