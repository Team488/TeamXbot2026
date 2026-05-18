package competition.command_groups;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import competition.command_groups.vision.BaseDriveWithSimpleBezierCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.AutoLandmarks;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.pose.RefinedSwervePointPathPlanning;
import edu.wpi.first.math.geometry.Pose2d;
import xbot.common.logging.RobotAssertionManager;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.subsystems.pose.GameField;
import xbot.common.trajectory.XbotSwervePoint;

public class FirstDriveForCollectionCommand extends BaseDriveWithSimpleBezierCommand {
    private final AutoLandmarks autoLandmarks;
    private final PoseSubsystem pose;
    private final RefinedSwervePointPathPlanning pathPlanning;

    @Inject
    public FirstDriveForCollectionCommand(DriveSubsystem drive, PoseSubsystem pose,
            PropertyFactory pf, HeadingModule.HeadingModuleFactory headingModuleFactory,
            RobotAssertionManager robotAssertionManager, RefinedSwervePointPathPlanning pathPlanning, GameField gamefield,
            AutoLandmarks autoLandmarks) {
        super(drive, pose, pf, headingModuleFactory, robotAssertionManager);
        this.pose = pose;
        this.pathPlanning = pathPlanning;
        this.autoLandmarks = autoLandmarks;
    }

    @Override
    public void initialize() {
        var currentPose = this.pose.getCurrentPose2d();
        var startPose = this.autoLandmarks.getStartCollectionPose(currentPose);
        var path = this.autoLandmarks.getMidBallPitCollectionPath(startPose);

        super.logic.setKeyPoints(this.pathPlanning.generateSwervePoints(startPose, path, false, true));
        super.setSegmentType(SegmentType.Mid);
        super.setMaxSpeed(MaxSpeed.Intake);

        super.initialize();
    }
}
