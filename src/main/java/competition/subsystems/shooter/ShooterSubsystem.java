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

    public DoubleProperty outputPower;
    public DoubleProperty targetVelocity;

    @Inject
    public ShooterSubsystem(XCANMotorController.XCANMotorControllerFactory xcanMotorControllerFactory,
                            ElectricalContract eletricalContract, PropertyFactory propertyFactory) {
        propertyFactory.setPrefix(this);
        if (eletricalContract.isShooterReady()) {
            shooterMotor = xcanMotorControllerFactory.create(eletricalContract.getShooterMotor(),
                    getPrefix(),"ShooterMotorPID",
                    new XCANMotorControllerPIDProperties(
                            0.1,
                            0.01,
                            0.25,
                            0.0002,
                            0.750,
                            1,
                            0));
        } else {
            this.shooterMotor = null;
        }

        this.outputPower = propertyFactory.createPersistentProperty("Output Power", 0.1);
        this.targetVelocity = propertyFactory.createPersistentProperty("Target Velocity", 3000);
    }
    public void output() {
        if (shooterMotor != null) {
            shooterMotor.setPower(outputPower.get());
        }
    }

    public void stop() {
        if (shooterMotor != null) {
            shooterMotor.setPower(0);
        }
    }

    public void increaseTargetVelocity() {
        targetVelocity.set(targetVelocity.get() + 25);
    }

    public void decreaseTargetVelocity() {
        targetVelocity.set(targetVelocity.get() - 25);
    }

    public void periodic() {
        if (shooterMotor != null) {
            shooterMotor.periodic();

            shooterMotor.setVelocityTarget(RPM.of(targetVelocity.get()));
        }
    }
}