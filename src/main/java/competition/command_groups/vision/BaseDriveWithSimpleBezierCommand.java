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

    private final DriveSubsystem drive;
    private final BooleanProperty useGlobalKinematics;
    private MaxSpeed maxSpeed = MaxSpeed.Default;
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

    protected void setPrioritizeRotationIfCloseToGoal(boolean prioritize) {
        this.prioritizeRotationIfCloseToGoal = prioritize;
    }

    @Override
    public void initialize() {
        this.logic.setPrioritizeRotationIfCloseToGoal(this.prioritizeRotationIfCloseToGoal);
        double speed = this.drive.getMaxTargetSpeedMetersPerSecond();
        switch (this.maxSpeed) {
        case Auto:
            speed = this.drive.getMaxAutoTargetSpeedMetersPerSecond();
            break;
        case Intake:
            speed = this.drive.getMaxAutoFuelIntakeTargetSpeedMetersPerSecond();
            break;
        case Default:
        default:
            speed = this.drive.getMaxTargetSpeedMetersPerSecond();
            break;
        }

        // Bug in simulation for global kinematic values.
        if (this.useGlobalKinematics.get() && !BaseRobot.isSimulation()) {
            this.logic.setVelocityMode(SwerveSimpleTrajectoryMode.GlobalKinematicsValue);
            super.logic.setGlobalKinematicValues(
                    new SwervePointKinematics(this.drive.getMaxAccelerationMetersPerSecondSquared(), 0, 0,
                            speed));
        } else {
            this.logic.setVelocityMode(SwerveSimpleTrajectoryMode.ConstantVelocity);
            this.logic.setConstantVelocity(speed);
        }

        super.initialize();
    }
}
