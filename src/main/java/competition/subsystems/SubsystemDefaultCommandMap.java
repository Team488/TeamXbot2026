package competition.subsystems;

import javax.inject.Inject;
import javax.inject.Singleton;

import competition.subsystems.climber.ClimberSubsystem;
import competition.subsystems.climber.commands.ClimberStopCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.SwerveDriveWithJoysticksCommand;
import competition.subsystems.fuel_intake.IntakeSubsystem;
import competition.subsystems.fuel_intake.commands.FuelStopCommand;
import competition.subsystems.intake_deploy.IntakeDeployStopCommand;
import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.shooter.commands.ShooterStopCommand;
import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.hood.commands.StopHoodCommand;
import competition.subsystems.intake_deploy.IntakeDeployExtendCommand;
import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
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

    @Inject
    public void setupShooterSubsystem(ShooterSubsystem shooter, ShooterStopCommand command) {
        shooter.setDefaultCommand(command);
    }

    @Inject
    public void setupHoodSubsystem(HoodSubsystem hood, StopHoodCommand command) {
        hood.setDefaultCommand(command);
    }

    @Inject
    public void climberStopCommand(ClimberSubsystem climberSubsystem, ClimberStopCommand command) {
        climberSubsystem.setDefaultCommand(command);
    }

    @Inject
    public void shooterStopCommand(ShooterSubsystem shooterSubsystem, ShooterStopCommand command) {
        shooterSubsystem.setDefaultCommand(command);
    }

    @Inject
    public void disableShooterFeederCommand(ShooterFeederSubsystem shooterFeederSubsystem,
                                            DisableShooterFeederCommand command) {
        shooterFeederSubsystem.setDefaultCommand(command);
    }

    @Inject
    public void intakeDeployStopCommand(IntakeDeploySubsystem intakeDeploySubsystem, IntakeDeployStopCommand command) {
        intakeDeploySubsystem.setDefaultCommand(command);
    }


}
