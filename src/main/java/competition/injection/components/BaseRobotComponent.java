package competition.injection.components;

import competition.auto_programs.ShootFromTrenchCommandGroup;
import competition.auto_programs.choreo.ChoreoAutoManager;
import competition.operator_interface.OperatorCommandMap;
import competition.operator_interface.OperatorInterface;
import competition.simulation.BaseSimulator;
import competition.subsystems.SubsystemDefaultCommandMap;
import competition.subsystems.climber.ClimberSubsystem;
import competition.subsystems.collector_intake.CollectorSubsystem;
import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import competition.subsystems.lights.LightsSubsystem;
import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.shooter.commands.WhenShooterReadyRumbleCommand;
import competition.subsystems.shooter_feeder.ShooterFeederSubsystem;
import competition.subsystems.voltage_alert.VoltageMonitorSubsystem;
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

    public abstract CollectorSubsystem collectorSubsystem();

    public abstract HopperRollerSubsystem hopperRollerSubsystem();

    public abstract VoltageMonitorSubsystem voltageMonitorSubsystem();

    public abstract ClimberSubsystem climberSubsystem();

    public abstract ChoreoAutoManager choreoAutoManager();

    public abstract ShootFromTrenchCommandGroup shootFromTrenchCommandGroup();

    public abstract OperatorInterface operatorInterface();

    public abstract WhenShooterReadyRumbleCommand whenShooterReadyRumbleCommand();
}
