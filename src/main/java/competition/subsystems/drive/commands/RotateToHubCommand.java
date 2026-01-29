package competition.subsystems.drive.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.vision.AprilTagVisionSubsystemExtended;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;
import xbot.common.command.BaseCommand;
import xbot.common.math.PIDManager;
import xbot.common.math.XYPair;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.Property;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.subsystems.drive.control_logic.HeadingModule.HeadingModuleFactory;

import javax.inject.Inject;

public class RotateToHubCommand extends BaseCommand {

    final DriveSubsystem drive;
    final PoseSubsystem pose;
    final HeadingModule headingModule;
    final PIDManager pidManager;
    final OperatorInterface oi;
    final PropertyFactory pf;
    final AprilTagFieldLayout fieldLayout;
    final AprilTagVisionSubsystemExtended vision;

    private static final int[] RED_HUB_TAG_IDS = {2, 3, 4, 5, 8, 9, 10, 11};
    private static final int[] BLUE_HUB_TAG_IDS = {18, 19, 20, 21, 24, 25, 26, 27};

    private Translation2d lockedTarget;
    private double targetAngle;
    private DoubleProperty angleToleranceDegrees;
    private int[] hubTagIds;

    @Inject
    public RotateToHubCommand(DriveSubsystem drive, PoseSubsystem pose,
                              HeadingModuleFactory headingFactory,
                              OperatorInterface oi,
                              PropertyFactory pf,
                              AprilTagFieldLayout fieldLayout,
                              AprilTagVisionSubsystemExtended vision) {
        this.drive = drive;
        this.pose = pose;
        this.oi = oi;
        this.pf = pf;
        this.fieldLayout = fieldLayout;
        this.vision = vision;
        this.pidManager = drive.getRotateToHeadingPid();
        this.headingModule = headingFactory.create(pidManager);

        pf.setPrefix(this);
        pf.setDefaultLevel(Property.PropertyLevel.Important);
        this.angleToleranceDegrees = pf.createPersistentProperty("AngleToleranceDegrees", 3.0);

        this.addRequirements(drive);
    }

    @Override
    public void initialize() {
        var alliance = DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue);
        hubTagIds = (alliance == DriverStation.Alliance.Red) ? RED_HUB_TAG_IDS : BLUE_HUB_TAG_IDS;
        lockedTarget = computeHubCenterFromVisibleTags(hubTagIds);

        if (lockedTarget != null) {
            Translation2d robotPos = pose.getCurrentPose2d().getTranslation();
            Translation2d vectorToHub = lockedTarget.minus(robotPos);
            targetAngle = vectorToHub.getAngle().getDegrees() + 180;
        }

        pidManager.reset();
        headingModule.reset();
    }

    @Override
    public void execute() {
        Translation2d computedTarget = computeHubCenterFromVisibleTags(hubTagIds);
        if (computedTarget != null) {
            lockedTarget = computedTarget;
        }

        if (lockedTarget == null) {
            return;
        }

        Translation2d robotPos = pose.getCurrentPose2d().getTranslation();
        Translation2d vectorToHub = lockedTarget.minus(robotPos);
        targetAngle = vectorToHub.getAngle().getDegrees() + 180;

        double rotationalPower = headingModule.calculateHeadingPower(targetAngle);

        drive.fieldOrientedDrive(
                new XYPair(oi.driverGamepad.getLeftStickY(), oi.driverGamepad.getRightStickX()),
                rotationalPower,
                pose.getCurrentHeading().getDegrees(),
                true
        );
    }

    @Override
    public boolean isFinished() {
        if (lockedTarget == null) {
            return true;
        }

        double currentHeading = pose.getCurrentHeading().getDegrees();
        double error = MathUtil.inputModulus(targetAngle - currentHeading, -180, 180);

        return Math.abs(error) < angleToleranceDegrees.get();
    }

    /**
     * Computes the hub center by averaging the FIELD poses (X/Y) of any hub-surrounding tags
     * that are currently visible by any camera.
     */
    private Translation2d computeHubCenterFromVisibleTags(int[] tagIds) {
        if (tagIds == null) {
            return null;
        }

        double sumX = 0;
        double sumY = 0;
        int count = 0;

        for (int id : tagIds) {
            if (!vision.tagVisibleByAnyCamera(id)) {
                continue;
            }
            var poseOpt = fieldLayout.getTagPose(id);
            if (poseOpt.isEmpty()) {
                continue;
            }
            var tagPose = poseOpt.get();
            sumX += tagPose.getX();
            sumY += tagPose.getY();
            count++;
        }

        if (count == 0) {
            return null;
        }

        return new Translation2d(sumX / count, sumY / count);
    }
}
// look at hub coordinates, where to find:
// Look for the 4 april tags that center each hub (both red and blue)
// Look up these april tags from AprilTagFieldLayout and then take the same x and same y.
// Determine which alliance we are and use the corresponding hub coordinates
// Direct the robot in that direction, and continue until it is orientated in that direction
// Also consider whether we are in the alliance area or not
// Map the button to something on the controller or keyboard in the sim then test in the sim
