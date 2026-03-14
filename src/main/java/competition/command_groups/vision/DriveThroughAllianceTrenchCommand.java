package competition.command_groups.vision;

import javax.inject.Inject;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.AutoLandmarks;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.pose.RefinedSwervePointPathPlanning;
import xbot.common.logging.RobotAssertionManager;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.control_logic.HeadingModule;

public class DriveThroughAllianceTrenchCommand extends BaseDriveWithSimpleBezierCommand {
    private final AutoLandmarks autoLandmarks;
    private final PoseSubsystem pose;
    private final RefinedSwervePointPathPlanning pathPlanning;

    @Inject
    public DriveThroughAllianceTrenchCommand(DriveSubsystem drive, PoseSubsystem pose,
            PropertyFactory pf, HeadingModule.HeadingModuleFactory headingModuleFactory,
            RobotAssertionManager robotAssertionManager, RefinedSwervePointPathPlanning pathPlanning,
            AutoLandmarks autoLandmarks) {
        super(drive, pose, pf, headingModuleFactory, robotAssertionManager);
        this.pose = pose;
        this.pathPlanning = pathPlanning;
        this.autoLandmarks = autoLandmarks;
    }

    @Override
    public void initialize() {
        var currentPose = this.pose.getCurrentPose2d();
        var path = this.autoLandmarks.getRelevantAlliancePathThroughTrench(currentPose);
        super.logic.setKeyPoints(this.pathPlanning.generateSwervePoints(currentPose, path, false));

        super.initialize();
    }
}
