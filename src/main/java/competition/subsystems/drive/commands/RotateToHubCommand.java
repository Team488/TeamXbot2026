package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.Landmarks;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.DriverStation;
import xbot.common.command.BaseCommand;
import xbot.common.properties.BooleanProperty;
import xbot.common.properties.Property;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import java.util.concurrent.locks.Lock;

public class RotateToHubCommand extends BaseCommand {

    final DriveSubsystem drive;
    final PoseSubsystem pose;
    final PropertyFactory pf;

    /**
     * Field layout containing AprilTag positions.
     */
    private final AprilTagFieldLayout aprilTagFieldLayout;

    private Pose2d targetPose;
    private final BooleanProperty AutoAimWhenNotInZone;

    @Inject
    public RotateToHubCommand(DriveSubsystem drive, PoseSubsystem pose, AprilTagFieldLayout aprilTagFieldLayout,
                              PropertyFactory pf) {
        this.drive = drive;
        this.pose = pose;
        this.aprilTagFieldLayout = aprilTagFieldLayout;
        this.pf = pf;
        pf.setPrefix(this);
        pf.setDefaultLevel(Property.PropertyLevel.Important);
        AutoAimWhenNotInZone = pf.createPersistentProperty("AutoAimWhenNotInZone", true);
    }

    @Override
    public void initialize() {
        DriverStation.Alliance alliance = DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue);
        targetPose = Landmarks.getAllianceHub(this.aprilTagFieldLayout, alliance);
    }

    @Override
    public void execute() {
        DriverStation.Alliance alliance = DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue);
        drive.setLookAtPointTarget(targetPose.getTranslation());
        double xTrenchLocation = 0.0;
        try {
            xTrenchLocation = Landmarks.getTrenchDriverDepotSideFiducialId(this.aprilTagFieldLayout, alliance).getX();
        } catch (Exception ignored) {
        }
        boolean areWeInAllianceZone = false;
        if (alliance == DriverStation.Alliance.Blue) {
            areWeInAllianceZone = pose.getCurrentPose2d().getX() >= xTrenchLocation;
        } else {
            areWeInAllianceZone = pose.getCurrentPose2d().getX() <= xTrenchLocation;
        }

        drive.setLookAtPointTargetActive(areWeInAllianceZone || AutoAimWhenNotInZone.get());
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
        drive.setLookAtPointTargetActive(false);
    }
}

