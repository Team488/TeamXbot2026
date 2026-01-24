package competition.injection.components;

import competition.operator_interface.OperatorCommandMap;
import competition.simulation.BaseSimulator;
import competition.subsystems.SubsystemDefaultCommandMap;
import competition.subsystems.lights.LightsSubsystem;
import competition.subsystems.shooter.ShooterSubsystem;
import xbot.common.injection.components.BaseComponent;
import xbot.common.injection.swerve.SwerveComponentHolder;
import xbot.common.subsystems.drive.swerve.SwerveDefaultCommandMap;
import competition.subsystems.vision.AprilTagVisionSubsystemExtended;

public abstract class BaseRobotComponent extends BaseComponent {
    public abstract SubsystemDefaultCommandMap subsystemDefaultCommandMap();

    public abstract OperatorCommandMap operatorCommandMap();

    public abstract SwerveDefaultCommandMap swerveDefaultCommandMap();

    public abstract SwerveComponentHolder swerveComponentHolder();

    public abstract AprilTagVisionSubsystemExtended aprilTagVisionSubsystemExtended();

    public abstract BaseSimulator simulator();

    public abstract ShooterSubsystem shooterSubsystem();

    public abstract LightsSubsystem lightsSubsystem();
}
