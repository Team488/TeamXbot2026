package competition.subsystems;

import javax.inject.Inject;
import javax.inject.Singleton;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.SwerveDriveWithJoysticksCommand;
import competition.subsystems.drive.commands.TankDriveWithJoysticksCommand;
import competition.subsystems.fuel_intake.IntakeSubsystem;
import competition.subsystems.fuel_intake.commands.FuelIntakeCommand;
import competition.subsystems.fuel_intake.commands.FuelStopCommand;
import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.shooter.commands.ShooterStopCommand;

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
    public void setupShooterSubsystem(ShooterSubsystem shooter, ShooterStopCommand command) {
        shooter.setDefaultCommand(command);
    }
}
