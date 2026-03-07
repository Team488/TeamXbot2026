package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import xbot.common.command.BaseCommand;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

public class DriveToSafeShootingDistance extends BaseCommand {
    DriveSubsystem drive;
    PoseSubsystem pose;
    PropertyFactory pf;

    public static double target = 0.5;
    public static final double Kp = 0.25;

    @Inject
    public DriveToSafeShootingDistance(DriveSubsystem drive, PoseSubsystem pose,
                                       PropertyFactory pf) {
        this.drive = drive;
        this.pose = pose;
        this.pf = pf;
        this.addRequirements(drive);
        pf.setPrefix(this);

    }

    public void initialize() {

    }

    public void execute() {

    }

    public void periodic() {

    }

    public void end() {

    }
}