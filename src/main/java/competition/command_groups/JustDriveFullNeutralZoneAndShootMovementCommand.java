package competition.command_groups;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import competition.command_groups.vision.BaseDriveWithSimpleBezierCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.AutoLandmarks;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.pose.RefinedSwervePointPathPlanning;
import xbot.common.injection.electrical_contract.XSwerveDriveElectricalContract;
import xbot.common.logging.RobotAssertionManager;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.trajectory.XbotSwervePoint;

public class JustDriveFullNeutralZoneAndShootMovementCommand extends BaseDriveWithSimpleBezierCommand {
    private final PoseSubsystem pose;
    private final RefinedSwervePointPathPlanning pathPlanning;
    private final AutoLandmarks autoLandmarks;

    @Inject
    public JustDriveFullNeutralZoneAndShootMovementCommand(DriveSubsystem drive, PoseSubsystem pose,
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
        List<XbotSwervePoint> swervePoints = new ArrayList<>();
        var startPathPoses = this.autoLandmarks.getStartCollectionPath(currentPose);
        swervePoints.addAll(this.pathPlanning.generateSwervePoints(currentPose, startPathPoses,
                false));

        var midPose = this.autoLandmarks.getMidBallPitCollectionPose(currentPose);
        var endCollectionPose = this.autoLandmarks.getFinishBallPitCollectionPose(currentPose);
        swervePoints.add(new XbotSwervePoint(midPose, 0.01));
        swervePoints.add(new XbotSwervePoint(endCollectionPose, 0.01));

        var pathPoses = this.autoLandmarks.getAllianceShootingStartingPath(currentPose);
        swervePoints.addAll(this.pathPlanning.generateSwervePoints(endCollectionPose, pathPoses,
                false));

        var shootingPose = this.autoLandmarks.getClosestShootingPose(pathPoses.get(pathPoses.size() - 1));
        swervePoints.add(new XbotSwervePoint(shootingPose, 0.01));

        this.setMaxSpeed(MaxSpeed.Auto);
        super.logic.setKeyPoints(swervePoints);

        super.initialize();
    }
}
