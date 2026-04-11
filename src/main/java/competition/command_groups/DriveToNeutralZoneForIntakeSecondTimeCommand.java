package competition.command_groups;

import java.util.List;

import javax.inject.Inject;

import competition.command_groups.vision.BaseDriveWithSimpleBezierCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.AutoLandmarks;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.pose.RefinedSwervePointPathPlanning;
import edu.wpi.first.math.geometry.Pose2d;
import xbot.common.injection.electrical_contract.XSwerveDriveElectricalContract;
import xbot.common.logging.RobotAssertionManager;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.control_logic.HeadingModule;

public class DriveToNeutralZoneForIntakeSecondTimeCommand extends BaseDriveWithSimpleBezierCommand {
    private final PoseSubsystem pose;
    private final RefinedSwervePointPathPlanning pathPlanning;
    private final AutoLandmarks autoLandmarks;

    @Inject
    public DriveToNeutralZoneForIntakeSecondTimeCommand(DriveSubsystem drive, PoseSubsystem pose,
            PropertyFactory pf, HeadingModule.HeadingModuleFactory headingModuleFactory,
            XSwerveDriveElectricalContract electricalContract,
            RobotAssertionManager robotAssertionManager, RefinedSwervePointPathPlanning pathPlanning,
            AutoLandmarks autoLandmarks) {
        super(drive, pose, pf, headingModuleFactory, robotAssertionManager);

        this.pose = pose;
        this.pathPlanning = pathPlanning;
        this.autoLandmarks = autoLandmarks;
    }

    @Override
    public void initialize() {
        var currentPose = pose.getCurrentPose2d();
        var pathPoses = this.autoLandmarks.getNearestAllianceToNeutralTrenchPath(currentPose);

        super.setSegmentType(SegmentType.Start);
        super.logic.setKeyPoints(this.pathPlanning.generateSwervePoints(currentPose, pathPoses,
                                                                        false, true));

        super.initialize();
    }
}
