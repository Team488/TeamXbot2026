package competition.subsystems.drive.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.Landmarks;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;
import xbot.common.command.BaseCommand;
import xbot.common.math.PIDManager;
import xbot.common.math.XYPair;
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

    private Pose2d targetPose;

    @Inject
    public RotateToHubCommand(DriveSubsystem drive, PoseSubsystem pose,
                              HeadingModuleFactory headingFactory,
                              OperatorInterface oi,
                              PropertyFactory pf) {
        this.drive = drive;
        this.pose = pose;
        this.oi = oi;
        this.pf = pf;
        this.pidManager = drive.getRotateToHeadingPid();
        this.headingModule = headingFactory.create(pidManager);

        pf.setPrefix(this);
        pf.setDefaultLevel(Property.PropertyLevel.Important);

        this.addRequirements(drive);
    }

    @Override
    public void initialize() {
        var alliance = DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue);

        if (alliance == DriverStation.Alliance.Blue) {
            targetPose = Landmarks.blueHub;
        } else {
            targetPose = PoseSubsystem.convertBlueToRedIfNeeded(Landmarks.blueHub);
        }

        pidManager.reset();
        headingModule.reset();
    }

    @Override
    public void execute() {
        Pose2d robotPos = pose.getCurrentPose2d();
        Translation2d vectorToHub = targetPose.getTranslation().minus(robotPos.getTranslation());
        double targetAngle = vectorToHub.getAngle().getDegrees() + 180;

        double rotationalPower = headingModule.calculateHeadingPower(targetAngle);

        drive.fieldOrientedDrive(
                new XYPair(oi.driverGamepad.getLeftStickY(), oi.driverGamepad.getRightStickX()),
                rotationalPower,
                pose.getCurrentHeading().getDegrees(),
                true
        );
    }
}
// look at hub coordinates, where to find --> landmarks
// Determine which alliance we are and use the corresponding hub coordinates
// Direct the robot in that direction, and continue until it is orientated in that direction
// Also consider whether we are in the alliance area or not
// Map the button to something on the controller or keyboard in the sim then test in the sim
