package competition.subsystems.shooter;

import competition.electrical_contract.ElectricalContract;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.controls.actuators.XCANMotorControllerPIDProperties;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.RPM;

@Singleton
public class ShooterSubsystem extends BaseSubsystem {
    public final XCANMotorController shooterMotor;

    public DoubleProperty targetVelocity;

    @Inject
    public ShooterSubsystem(XCANMotorController.XCANMotorControllerFactory xcanMotorControllerFactory,
                            ElectricalContract electricalContract, PropertyFactory propertyFactory) {
        propertyFactory.setPrefix(this);
        if (electricalContract.isShooterReady()) {
            shooterMotor = xcanMotorControllerFactory.create(electricalContract.getShooterMotor(),
                    getPrefix(),"ShooterMotorPID",
                    new XCANMotorControllerPIDProperties(
                            0.1,
                            0.01,
                            0.25,
                            0.0002,
                            0.750,
                            1,
                            0));
            registerDataFrameRefreshable(shooterMotor);
        } else {
            this.shooterMotor = null;
        }

        this.targetVelocity = propertyFactory.createPersistentProperty("Target Velocity", 3000);
    }

    public void runAtTargetVelocity() {
        shooterMotor.setVelocityTarget(RPM.of(targetVelocity.get()));
    }

    public void setTargetVelocity(double velocity) {
        targetVelocity.set(velocity);
        shooterMotor.setVelocityTarget(RPM.of(targetVelocity.get()));
    }

    public void stop() {
        if (shooterMotor != null) {
            shooterMotor.setPower(0);
        }
    }

    public void increaseTargetVelocity() {
        targetVelocity.set(targetVelocity.get() + 25);
        shooterMotor.setVelocityTarget(RPM.of(targetVelocity.get()));
    }

    public void decreaseTargetVelocity() {
        targetVelocity.set(targetVelocity.get() - 25);
        shooterMotor.setVelocityTarget(RPM.of(targetVelocity.get()));
    }

    public void periodic() {
        if (shooterMotor != null) {
            shooterMotor.periodic();
        }
    }
}