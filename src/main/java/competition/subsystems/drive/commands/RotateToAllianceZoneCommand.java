package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.Landmarks;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.units.Units;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import xbot.common.command.BaseCommand;
import xbot.common.properties.AngleProperty;
import xbot.common.properties.BooleanProperty;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.Property;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

public class RotateToAllianceZoneCommand extends BaseCommand {

    final DriveSubsystem drive;
    final PoseSubsystem pose;
    final PropertyFactory pf;

    /**
     * Field layout containing AprilTag positions.
     */
    private final AprilTagFieldLayout aprilTagFieldLayout;

    private Pose2d robotPose;
    private final DoubleProperty interpolationFactor;
    private final BooleanProperty autoAimWhenNotInNeutralZone;

    @Inject
    public RotateToAllianceZoneCommand(DriveSubsystem drive, PoseSubsystem pose, AprilTagFieldLayout aprilTagFieldLayout,
                                       PropertyFactory pf) {
        this.drive = drive;
        this.pose = pose;
        this.aprilTagFieldLayout = aprilTagFieldLayout;
        this.pf = pf;
        pf.setPrefix(this);
        pf.setDefaultLevel(Property.PropertyLevel.Important);
        interpolationFactor = pf.createPersistentProperty("InterpolationFactor", 0.5);
        autoAimWhenNotInNeutralZone = pf.createPersistentProperty("AutoAimWhenNotInNeutralZone", true);
    }

    @Override
    public void initialize() {
        log.info("Initializing");
        robotPose = pose.getCurrentPose2d();
        Alliance alliance = DriverStation.getAlliance().orElse(Alliance.Blue);
        Pose2d closestTrenchNeutralSideIdPose = Landmarks.getClosestTrenchNeutralSideIdPose(
                aprilTagFieldLayout,
                alliance,
                robotPose
        );

        // Interpolation instead of a fixed point prevents shooting into the hub wall when we are too close to the hub
        // Uses .getTranslation() for linear interpolation because the interpolation method for poses has some issues
        Translation2d targetTranslation = Landmarks.getAllianceHubPose(this.aprilTagFieldLayout, alliance)
                .getTranslation()
                .interpolate(closestTrenchNeutralSideIdPose.getTranslation(), interpolationFactor.get());

        drive.setLookAtPointTarget(targetTranslation);
        drive.setLookAtPointInverted(true);
    }

    @Override
    public void execute() {
        boolean areWeInNeutralZone = Landmarks.isBetweenIdX(
                this.aprilTagFieldLayout,
                Landmarks.getAllianceHubNeutralSideFiducialId(Alliance.Blue),
                Landmarks.getAllianceHubNeutralSideFiducialId(Alliance.Red),
                robotPose
        );

        drive.setLookAtPointTargetActive(areWeInNeutralZone || autoAimWhenNotInNeutralZone.get());
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
        drive.setStaticHeadingTargetActive(false);
        drive.setLookAtPointInverted(false);
    }
}