package competition.subsystems;

import javax.inject.Inject;
import javax.inject.Singleton;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.SwerveDriveWithJoysticksCommand;
import competition.subsystems.drive.commands.TankDriveWithJoysticksCommand;

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
