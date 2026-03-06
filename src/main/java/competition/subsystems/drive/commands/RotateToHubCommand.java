package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.Landmarks;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.units.Units;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import xbot.common.command.BaseCommand;
import xbot.common.properties.AngleProperty;
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
    private Rotation2d rotationOffset;
    private Translation2d targetTranslation;
    private final AngleProperty desiredHeadingOffset;
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
        desiredHeadingOffset = pf.createPersistentProperty("DesiredHeadingOffset", Units.Degrees.of(180));
        autoAimWhenNotInZone = pf.createPersistentProperty("AutoAimWhenNotInZone", true);
    }

    @Override
    public void initialize() {
        log.info("Initializing");
        alliance = DriverStation.getAlliance().orElse(Alliance.Blue);
        rotationOffset = Rotation2d.fromDegrees(desiredHeadingOffset.get().in(Units.Degrees));
        targetTranslation = Landmarks.getAllianceHubPose(this.aprilTagFieldLayout, alliance).getTranslation();
        drive.setLookAtPointTarget(targetTranslation);
        drive.setLookAtPointInverted(true);

    }

    @Override
    public void execute() {
        boolean areWeInAllianceZone = Landmarks.isBetweenIdX(
                this.aprilTagFieldLayout,
                Landmarks.getTrenchDriverDepotSideId(alliance),
                Landmarks.getOutpostFiducialId(alliance),
                pose.getCurrentPose2d()
        );

        drive.setLookAtPointTargetActive(areWeInAllianceZone || autoAimWhenNotInZone.get());
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
        drive.setLookAtPointTargetActive(false);
        drive.setLookAtPointInverted(false);
    }
}
