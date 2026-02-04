package competition.subsystems.shooter;

import competition.electrical_contract.ElectricalContract;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.controls.actuators.XCANMotorControllerPIDProperties;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

import static edu.wpi.first.units.Units.RPM;

@Singleton
public class ShooterSubsystem extends BaseSubsystem {
    public final XCANMotorController leftShooterMotor;
    public final XCANMotorController middleShooterMotor;
    public final XCANMotorController rightShooterMotor;

    public final List<XCANMotorController> shooterMotors = new ArrayList<>();

    public DoubleProperty targetVelocity;

    @Inject
    public ShooterSubsystem(XCANMotorController.XCANMotorControllerFactory xcanMotorControllerFactory,
                            ElectricalContract eletricalContract,
                            PropertyFactory propertyFactory) {

        propertyFactory.setPrefix(this);

        var defaultPIDProperties = new XCANMotorControllerPIDProperties(
                0.1,
                0.01,
                0.25,
                0.0002,
                0.750,
                1,
                0);

        if (eletricalContract.isLeftShooterReady()) {
            this.leftShooterMotor = xcanMotorControllerFactory.create(eletricalContract.getLeftShooterMotor(),
                    getPrefix(), "ShooterMotor", defaultPIDProperties);
            shooterMotors.add(leftShooterMotor);
            this.registerDataFrameRefreshable(leftShooterMotor);
        } else {
            this.leftShooterMotor = null;
        }

        if (eletricalContract.isMiddleShooterReady()) {
            this.middleShooterMotor = xcanMotorControllerFactory.create(eletricalContract.getMiddleShooterMotor(),
                    getPrefix(), "ShooterMotor", defaultPIDProperties);
            shooterMotors.add(middleShooterMotor);
            this.registerDataFrameRefreshable(middleShooterMotor);
        } else {
            this.middleShooterMotor = null;
        }

        if (eletricalContract.isRightShooterReady()) {
            this.rightShooterMotor = xcanMotorControllerFactory.create(eletricalContract.getRightShooterMotor(),
                    getPrefix(), "ShooterMotor", defaultPIDProperties);
            shooterMotors.add(rightShooterMotor);
            this.registerDataFrameRefreshable(rightShooterMotor);
        } else {
            this.rightShooterMotor = null;
        }

        this.targetVelocity = propertyFactory.createPersistentProperty("Target Velocity", 3000);
    }

    public void stop() {
        for (var motor : shooterMotors) {
            motor.setPower(0);
        }
    }

    public void increaseTargetVelocity() {
        targetVelocity.set(targetVelocity.get() + 25);
    }

    public void decreaseTargetVelocity() {
        targetVelocity.set(targetVelocity.get() - 25);

    }

    public void setTargetVelocity(double velocity) {
        targetVelocity.set(velocity);
    }

    public void runAtTargetVelocity() {
        for (var motor : shooterMotors) {
            motor.setVelocityTarget(RPM.of(targetVelocity.get()));
        }
    }

    public void periodic() {
        for (var motor : shooterMotors) {
            motor.periodic();
            motor.setVelocityTarget(RPM.of(targetVelocity.get()));
        }
    }
}