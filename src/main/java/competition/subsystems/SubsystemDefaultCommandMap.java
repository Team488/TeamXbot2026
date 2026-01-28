package competition.subsystems;

import javax.inject.Inject;
import javax.inject.Singleton;

import competition.subsystems.climber.ClimberSubsystem;
import competition.subsystems.climber.commands.ClimberStopCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.SwerveDriveWithJoysticksCommand;
import competition.subsystems.fuel_intake.IntakeSubsystem;
import competition.subsystems.fuel_intake.commands.FuelStopCommand;
import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.hood.commands.StopHoodCommand;
import competition.subsystems.intake_deploy.IntakeDeployStopCommand;
import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.shooter.commands.ShooterStopCommand;
import competition.subsystems.shooter_feeder.ShooterFeederSubsystem;
import competition.subsystems.shooter_feeder.commands.DisableShooterFeederCommand;

/**
 * For setting the default commands on subsystems
 */
@Singleton
public class SubsystemDefaultCommandMap {

    @Inject
    public SubsystemDefaultCommandMap() {}

    @Inject
    public void setupDriveSubsystem(DriveSubsystem driveSubsystem, SwerveDriveWithJoysticksCommand command) {
        driveSubsystem.setDefaultCommand(command);
    }

    @Inject
    public void setupIntakeSubsystem(IntakeSubsystem intake, FuelStopCommand command) {
        intake.setDefaultCommand(command);
    }

    public void setupShooterSubsystem(ShooterSubsystem shooter, ShooterStopCommand command) {
        shooter.setDefaultCommand(command);
    }

    @Inject
    public void setupHoodSubsystem(HoodSubsystem hood, StopHoodCommand command) {
        hood.setDefaultCommand(command);
    }

    @Inject
    public void setupClimberSubsystem(ClimberSubsystem climber, ClimberStopCommand command) {
        climber.setDefaultCommand(command);
    }

    @Inject
    public void setupShooterFeederSubsystem(ShooterFeederSubsystem shooterFeeder, DisableShooterFeederCommand command) {
        shooterFeeder.setDefaultCommand(command);
    }

    @Inject
    public void intakeDeployStopCommand(IntakeSubsystem intakeSubsystem, IntakeDeployStopCommand command) {
        intakeSubsystem.setDefaultCommand(command);
    }
}
