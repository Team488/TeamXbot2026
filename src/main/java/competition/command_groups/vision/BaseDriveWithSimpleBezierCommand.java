package competition.command_groups.vision;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import xbot.common.command.BaseRobot;
import xbot.common.logging.RobotAssertionManager;
import xbot.common.properties.BooleanProperty;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.SwervePointKinematics;
import xbot.common.subsystems.drive.SwerveSimpleBezierCommand;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryMode;
import xbot.common.subsystems.drive.control_logic.HeadingModule;

public class BaseDriveWithSimpleBezierCommand extends SwerveSimpleBezierCommand {
    protected enum MaxSpeed {
        Default,
        Auto,
        Intake
    }

    protected enum EndpointSpeed {
        Stop,
        Interstitial
    }

    public enum SegmentType {
        Start,
        Mid,
        End,
        StartAndEnd
    }

    private final DriveSubsystem drive;
    private final BooleanProperty useGlobalKinematics;
    private MaxSpeed maxSpeed = MaxSpeed.Default;
    private EndpointSpeed startSpeed = EndpointSpeed.Stop;
    private EndpointSpeed endSpeed = EndpointSpeed.Stop;
    private boolean prioritizeRotationIfCloseToGoal = false;

    public BaseDriveWithSimpleBezierCommand(DriveSubsystem drive, PoseSubsystem pose,
            PropertyFactory pf, HeadingModule.HeadingModuleFactory headingModuleFactory,
            RobotAssertionManager robotAssertionManager) {
        super(drive, pose, pf, headingModuleFactory, robotAssertionManager);

        this.drive = drive;
        this.useGlobalKinematics = pf.createPersistentProperty("useGlobalKinematics", true);
    }

    protected void setMaxSpeed(MaxSpeed speed) {
        this.maxSpeed = speed;
    }

    public void setSegmentType(SegmentType segmentType) {
        switch (segmentType) {
        case Start:
            this.startSpeed = EndpointSpeed.Stop;
            this.endSpeed = EndpointSpeed.Interstitial;
            break;
        case End:
            this.startSpeed = EndpointSpeed.Interstitial;
            this.endSpeed = EndpointSpeed.Stop;
            break;
        case Mid:
            this.startSpeed = EndpointSpeed.Interstitial;
            this.endSpeed = EndpointSpeed.Interstitial;
            break;
        case StartAndEnd:
        default:
            this.startSpeed = EndpointSpeed.Stop;
            this.endSpeed = EndpointSpeed.Stop;
            break;
        }
    }

    protected void setPrioritizeRotationIfCloseToGoal(boolean prioritize) {
        this.prioritizeRotationIfCloseToGoal = prioritize;
    }

    private double getEndpointSpeed(EndpointSpeed speed) {
        switch (speed) {
        case Interstitial:
            return this.drive.getInterstitialSpeedMetersPerSecond();
        case Stop:
        default:
            return 0.0;
        }
    }

    @Override
    public void initialize() {
        this.logic.setPrioritizeRotationIfCloseToGoal(this.prioritizeRotationIfCloseToGoal);
        double maxSpeed = this.drive.getMaxTargetSpeedMetersPerSecond();
        switch (this.maxSpeed) {
        case Auto:
            maxSpeed = this.drive.getMaxAutoTargetSpeedMetersPerSecond();
            break;
        case Intake:
            maxSpeed = this.drive.getMaxAutoFuelIntakeTargetSpeedMetersPerSecond();
            break;
        case Default:
        default:
            maxSpeed = this.drive.getMaxTargetSpeedMetersPerSecond();
            break;
        }

        double startSpeed = this.getEndpointSpeed(this.startSpeed);
        double endSpeed = this.getEndpointSpeed(this.endSpeed);

        // Bug in simulation for global kinematic values.
        if (this.useGlobalKinematics.get() && !BaseRobot.isSimulation()) {
            this.logic.setVelocityMode(SwerveSimpleTrajectoryMode.GlobalKinematicsValue);
            super.logic.setGlobalKinematicValues(
                    new SwervePointKinematics(this.drive.getMaxAccelerationMetersPerSecondSquared(), startSpeed, endSpeed,
                            maxSpeed));
        } else {
            this.logic.setVelocityMode(SwerveSimpleTrajectoryMode.ConstantVelocity);
            this.logic.setConstantVelocity(maxSpeed);
        }

        super.initialize();
    }
}
