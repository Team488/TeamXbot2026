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
import competition.subsystems.hood.commands.HoodToGoalCommand;
import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import competition.subsystems.intake_deploy.commands.IntakeDeployMaintainerCommand;
import competition.subsystems.intake_deploy.commands.IntakeDeployRetractCommand;
import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import competition.subsystems.shooter.commands.ShooterStopCommand;
import competition.subsystems.shooter.commands.ShooterWheelMaintainerCommand;
import competition.subsystems.shooter_feeder.ShooterFeederSubsystem;
import competition.subsystems.shooter_feeder.commands.ShooterFeederStop;

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
    public void setupShooterSubsystem(ShooterSubsystem shooter,
                                      ShooterWheelMaintainerCommand command,
                                      ShooterStopCommand stopCommand) {
        shooter.setDefaultCommand(command);
        shooter.getSetpointLock().setDefaultCommand(stopCommand);
    }

    @Inject
    public void setupClimberSubsystem(ClimberSubsystem climber, ClimberStopCommand command) {
        climber.setDefaultCommand(command);
    }

    @Inject
    public void setupShooterFeederSubsystem(ShooterFeederSubsystem shooterFeeder, ShooterFeederStop command) {
        shooterFeeder.setDefaultCommand(command);
    }

    @Inject
    public void setupIntakeDeploySubsystem(IntakeDeploySubsystem intakeDeploy,
                                           IntakeDeployMaintainerCommand command,
                                           IntakeDeployRetractCommand retractCommand) {
        intakeDeploy.setDefaultCommand(command);
        intakeDeploy.getSetpointLock().setDefaultCommand(retractCommand);
    }

    @Inject
    public void setupHoodSubsystem(HoodSubsystem hood, HoodToGoalCommand command) {
        hood.setDefaultCommand(command);
    }

    @Inject
    public void setupHopperRollerSubsystem(HopperRollerSubsystem hopperRoller) {
        hopperRoller.setDefaultCommand(hopperRoller.getStopCommand());
    }
}
