package competition.subsystems;

import javax.inject.Inject;
import javax.inject.Singleton;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.SwerveDriveWithJoysticksCommand;
import competition.subsystems.fuel_intake.IntakeSubsystem;
import competition.subsystems.fuel_intake.commands.FuelStopCommand;
import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.shooter.commands.ShooterStopCommand;
import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.hood.commands.StopHoodCommand;

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
}
