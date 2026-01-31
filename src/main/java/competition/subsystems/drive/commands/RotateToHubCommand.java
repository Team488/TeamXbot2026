package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.Landmarks;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.units.Units;
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

    private Pose2d targetPose;
    private final BooleanProperty AutoAimWhenNotInZone;

    @Inject
    public RotateToHubCommand(DriveSubsystem drive, PoseSubsystem pose,
                              PropertyFactory pf) {
        this.drive = drive;
        this.pose = pose;
        this.pf = pf;
        pf.setPrefix(this);
        pf.setDefaultLevel(Property.PropertyLevel.Important);
        AutoAimWhenNotInZone = pf.createPersistentProperty("AutoAimWhenNotInZone", true);
    }

    @Override
    public void initialize() {
        targetPose = Landmarks.getAllianceHub(DriverStation.getAlliance());
    }

    @Override
    public void execute() {
        drive.setLookAtPointTarget(targetPose.getTranslation());
        double xTrenchLocation = Landmarks.getTrenchDriverDepotSideFiducialId(DriverStation.getAlliance());
        boolean areWeInAllianceZone = false;
        if (DriverStation.getAlliance() == DriverStation.Alliance.Blue) {
            areWeInAllianceZone = pose.getCurrentPose2d().getX() >= xTrenchLocation;
        } else {
            areWeInAllianceZone = pose.getCurrentPose2d().getX() <= xTrenchLocation;
        }

        drive.setLookAtPointTarget(areWeInAllianceZone || AutoAimWhenNotInZone.get());
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
        drive.setLookAtPointTargetActive(false);
    }
}

