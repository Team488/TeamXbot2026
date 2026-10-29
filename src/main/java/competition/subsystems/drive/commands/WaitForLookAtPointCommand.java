package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import xbot.common.command.BaseCommand;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

public class WaitForLookAtPointCommand extends BaseCommand {

    private final DriveSubsystem drive;
    private final PoseSubsystem pose;
    private final DoubleProperty rotationErrorThresholdInDegrees;

    @Inject
    public WaitForLookAtPointCommand(DriveSubsystem drive, PoseSubsystem pose, PropertyFactory pf) {
        this.drive = drive;
        this.pose = pose;
        pf.setPrefix(this);
        rotationErrorThresholdInDegrees = pf.createPersistentProperty("rotationErrorThresholdInDegrees", 3.0);
    }

    @Override
    public boolean isFinished() {
        Translation2d target = drive.getLookAtPointTarget();
        Pose2d currentPose = pose.getCurrentPose2d();

        // By default, we need to add 180 to our desiredHeading.
        double desiredHeading = currentPose.getTranslation().minus(target).getAngle().getDegrees() + 180;
        if (drive.getLookAtPointInverted()) {
            desiredHeading -= 180;
        }

        double delta = Math.abs(desiredHeading - currentPose.getRotation().getDegrees());
        return delta <= this.rotationErrorThresholdInDegrees.get();
    }
    
}
