package competition.injection.components;

import competition.operator_interface.OperatorCommandMap;
import competition.simulation.BaseSimulator;
import competition.subsystems.SubsystemDefaultCommandMap;
import competition.subsystems.climber.ClimberSubsystem;
import competition.subsystems.fuel_intake.IntakeSubsystem;
import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import competition.subsystems.lights.LightsSubsystem;
import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.shooter_feeder.ShooterFeederSubsystem;
import xbot.common.injection.components.BaseComponent;
import xbot.common.injection.swerve.SwerveComponentHolder;
import xbot.common.subsystems.drive.swerve.SwerveDefaultCommandMap;
import competition.subsystems.vision.AprilTagVisionSubsystemExtended;
import xbot.common.subsystems.pose.GameField;

public abstract class BaseRobotComponent extends BaseComponent {
    public abstract SubsystemDefaultCommandMap subsystemDefaultCommandMap();

    public abstract OperatorCommandMap operatorCommandMap();

    public abstract SwerveDefaultCommandMap swerveDefaultCommandMap();

    public abstract SwerveComponentHolder swerveComponentHolder();

    public abstract AprilTagVisionSubsystemExtended aprilTagVisionSubsystemExtended();

    public abstract BaseSimulator simulator();

    public abstract ShooterSubsystem shooterSubsystem();

    public abstract ShooterFeederSubsystem shooterFeederSubsystem();

    public abstract HoodSubsystem hoodSubsystem();

    public abstract GameField gameField();

    public abstract LightsSubsystem lightsSubsystem();

    public abstract IntakeDeploySubsystem intakeDeploySubsystem();

    public abstract IntakeSubsystem intakeSubsystem();

    public abstract HopperRollerSubsystem hopperRollerSubsystem();

}
