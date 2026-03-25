package competition.auto_programs.choreo;

import choreo.auto.AutoFactory;
import choreo.auto.AutoChooser;
import choreo.auto.AutoRoutine;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import xbot.common.math.XYPair;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import xbot.common.subsystems.pose.BasePoseSubsystem;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ChoreoAutoManager {

    private final AutoFactory autoFactory;
    private final AutoChooser autoChooser;
    private final DriveSubsystem drive;
    private final PoseSubsystem pose;

    @Inject
    public ChoreoAutoManager(DriveSubsystem drive, PoseSubsystem pose) {
        this.drive = drive;
        this.pose = pose;

        // Initialize AutoFactory
        this.autoFactory = new AutoFactory(
                pose::getCurrentPose2d,
                this::setCurrentPose,
                this::followTrajectory,
                true, // enableAllianceFlipping
                drive
        );

        this.autoChooser = new AutoChooser();
        
        setupAutos();
        
        SmartDashboard.putData("Choreo Auto Chooser", this.autoChooser);
    }
    
    private void setCurrentPose(Pose2d newPose) {
        // We have to split this into setCurrentPosition and setCurrentHeading
        pose.setCurrentPosition(
            newPose.getX() * BasePoseSubsystem.INCHES_IN_A_METER,
            newPose.getY() * BasePoseSubsystem.INCHES_IN_A_METER
        );
        pose.setCurrentHeading(newPose.getRotation().getDegrees());
    }
    
    private void followTrajectory(choreo.trajectory.SwerveSample sample) {
        double xSpeed = sample.vx;
        double ySpeed = sample.vy;
        double omega = sample.omega;
        
        double maxSpeed = drive.getMaxTargetSpeedMetersPerSecond();
        double maxTurn = drive.getMaxTargetTurnRate();
        
        XYPair translation = new XYPair(xSpeed / maxSpeed, ySpeed / maxSpeed);
        
        drive.fieldOrientedDrive(translation, omega / maxTurn, pose.getCurrentHeading().getDegrees(), new XYPair());
    }

    private void setupAutos() {
        // Register autos here
        // autoChooser.addRoutine("Example Auto", this::exampleAuto);
    }

    public AutoChooser getAutoChooser() {
        return autoChooser;
    }

    public AutoFactory getAutoFactory() {
        return autoFactory;
    }
}
