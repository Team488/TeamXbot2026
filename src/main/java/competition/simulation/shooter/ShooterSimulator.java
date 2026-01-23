package competition.simulation.shooter;

import competition.subsystems.shooter.ShooterSubsystem;
import edu.wpi.first.wpilibj.MockDigitalInput;
import xbot.common.controls.actuators.mock_adapters.MockCANMotorController;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ShooterSimulator {


    final ShooterSubsystem shooterSubsystem;
    final MockCANMotorController shooterMotor;

    @Inject
    public ShooterSimulator(ShooterSubsystem shooterSubsystem) {
        this.shooterSubsystem = shooterSubsystem;
        this.shooterMotor = (MockCANMotorController) shooterSubsystem.shooterMotor;
    }

    public boolean isShooting() {
        return shooterMotor.getPower() > 0;
    }
}
