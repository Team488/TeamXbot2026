package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.Landmarks;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.units.Units;
import xbot.common.command.BaseCommand;
import xbot.common.properties.Property;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

public class RotateToHubCommand extends BaseCommand {

    final DriveSubsystem drive;
    final PoseSubsystem pose;
    final PropertyFactory pf;

    private Pose2d targetPose;

    @Inject
    public RotateToHubCommand(DriveSubsystem drive, PoseSubsystem pose,
                              PropertyFactory pf) {
        this.drive = drive;
        this.pose = pose;
        this.pf = pf;
        pf.setPrefix(this);
        pf.setDefaultLevel(Property.PropertyLevel.Important);
    }

    @Override
    public void initialize() {
        targetPose = PoseSubsystem.convertBlueToRedIfNeeded(Landmarks.blueHub);
    }

    @Override
    public void execute() {
        drive.setLookAtPointTarget(targetPose.getTranslation());
        
        if (targetPose == Landmarks.blueHub) {
            if (pose.getCurrentPose2d().getX() <= Landmarks.blueAllianceArea.in(Units.Meters)) {
                drive.setLookAtPointTargetActive(true);
            }
        } else {
            if (pose.getCurrentPose2d().getX() >= (Landmarks.fieldLength.minus(Landmarks.blueAllianceArea)).in(Units.Meters)) {
                drive.setLookAtPointTargetActive(true);
            }
        }
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
        drive.setLookAtPointTargetActive(false);
    }
}

