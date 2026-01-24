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
    public void intakeStopCommand(IntakeSubsystem intakeSubsystem, FuelStopCommand command) {
        intakeSubsystem.setDefaultCommand(command);
    }

    @Inject
    public void hoodSubSystem(HoodSubsystem hoodSubsystem, StopHoodCommand command) {
        hoodSubsystem.setDefaultCommand(command);
    }
    @Inject
    public void climberStopCommand(ClimberSubsystem climberSubsystem, ClimberStopCommand command) {
        climberSubsystem.setDefaultCommand(command);
    }
    @Inject
    public void shooterStopCommand(ShooterSubsystem shooterSubsystem, ShooterStopCommand command) {
        shooterSubsystem.setDefaultCommand(command);
    }
    public void disableShooterFeederCommand(ShooterFeederSubsystem shooterFeederSubsystem,
                                            DisableShooterFeederCommand command) {
        shooterFeederSubsystem.setDefaultCommand(command);
    }


}
