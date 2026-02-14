package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.Landmarks;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import xbot.common.command.BaseCommand;
import xbot.common.properties.BooleanProperty;
import xbot.common.properties.Property;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

public class RotateToHubCommand extends BaseCommand {

    final DriveSubsystem drive;
    final PoseSubsystem pose;
    final PropertyFactory pf;

    /**
     * Field layout containing AprilTag positions.
     */
    private final AprilTagFieldLayout aprilTagFieldLayout;

    private Alliance alliance;
    private Pose2d targetPose;
    private final BooleanProperty autoAimWhenNotInZone;

    @Inject
    public RotateToHubCommand(DriveSubsystem drive, PoseSubsystem pose, AprilTagFieldLayout aprilTagFieldLayout,
                              PropertyFactory pf) {
        this.drive = drive;
        this.pose = pose;
        this.aprilTagFieldLayout = aprilTagFieldLayout;
        this.pf = pf;
        pf.setPrefix(this);
        pf.setDefaultLevel(Property.PropertyLevel.Important);
        autoAimWhenNotInZone = pf.createPersistentProperty("AutoAimWhenNotInZone", true);
    }

    @Override
    public void initialize() {
        alliance = DriverStation.getAlliance().orElse(Alliance.Blue);
        targetPose = Landmarks.getAllianceHubPose(this.aprilTagFieldLayout, alliance);
    }

    @Override
    public void execute() {
        drive.setLookAtPointTarget(targetPose.getTranslation());
        boolean areWeInAllianceZone = Landmarks.isBetweenIdX(
                this.aprilTagFieldLayout,
                Landmarks.getTrenchDriverDepotSideId(alliance),
                Landmarks.getOutpostId(alliance),
                pose.getCurrentPose2d()
        );

        drive.setLookAtPointTargetActive(areWeInAllianceZone || autoAimWhenNotInZone.get());
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
        drive.setLookAtPointTargetActive(false);
    }
}


