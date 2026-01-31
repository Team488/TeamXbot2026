package competition.subsystems.shooter;

import competition.electrical_contract.ElectricalContract;
import edu.wpi.first.units.AngularAccelerationUnit;
import edu.wpi.first.units.measure.AngularVelocity;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.controls.actuators.XCANMotorControllerPIDProperties;
import xbot.common.controls.actuators.XSpeedController;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.lang.annotation.Target;

import static edu.wpi.first.units.Units.RPM;

@Singleton
public class ShooterSubsystem extends BaseSetpointSubsystem<AngularVelocity, Double> {
    public final XCANMotorController leftShooterMotor;
    public final XCANMotorController middleShooterMotor;
    public final XCANMotorController rightShooterMotor;
        public ElectricalContract electricalContract;

    public DoubleProperty targetVelocity;
    double rotationAtZero = 0;

    @Inject
    public ShooterSubsystem(XCANMotorController.XCANMotorControllerFactory xcanMotorControllerFactory,
                            ElectricalContract electricalContract, PropertyFactory propertyFactory) {

        propertyFactory.setPrefix(this);
        this.electricalContract = electricalContract;

        var defaultPIDProperties = new XCANMotorControllerPIDProperties(
                0.1,
                0.01,
                0.25,
                0.0002,
                0.750,
                1,
                0);

        if (electricalContract.isLeftShooterReady()) {
            this.leftShooterMotor = xcanMotorControllerFactory.create(electricalContract.getLeftShooterMotor(),
                    getPrefix(), "ShooterMotor", defaultPIDProperties);
            this.registerDataFrameRefreshable(leftShooterMotor);
        } else {
            this.leftShooterMotor = null;
        }

        if (electricalContract.isMiddleShooterReady()) {
            this.middleShooterMotor = xcanMotorControllerFactory.create(electricalContract.getMiddleShooterMotor(),
                    getPrefix(), "ShooterMotor", defaultPIDProperties);
            this.registerDataFrameRefreshable(middleShooterMotor);
        } else {
            this.middleShooterMotor = null;
        }

        if (electricalContract.isRightShooterReady()) {
            this.rightShooterMotor = xcanMotorControllerFactory.create(electricalContract.getRightShooterMotor(),
                    getPrefix(), "ShooterMotor", defaultPIDProperties);
            this.registerDataFrameRefreshable(rightShooterMotor);
        } else {
            this.rightShooterMotor = null;
        }

        this.targetVelocity = propertyFactory.createPersistentProperty("Target Velocity", 3000);
    }

    public void stop() {
        if (leftShooterMotor != null) {
            leftShooterMotor.setPower(0);
        }

        if (middleShooterMotor != null) {
            middleShooterMotor.setPower(0);
        }

        if (rightShooterMotor != null) {
            rightShooterMotor.setPower(0);
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
        if (leftShooterMotor != null) {
            leftShooterMotor.setVelocityTarget(RPM.of(targetVelocity.get()));
        }

        if (middleShooterMotor != null) {
            middleShooterMotor.setVelocityTarget(RPM.of(targetVelocity.get()));
        }

        if (rightShooterMotor != null) {
            rightShooterMotor.setVelocityTarget(RPM.of(targetVelocity.get()));
        }
    }

    public void periodic() {
        if (leftShooterMotor != null) {
            leftShooterMotor.periodic();
            leftShooterMotor.setVelocityTarget(RPM.of(targetVelocity.get()));
        }

        if (middleShooterMotor != null) {
            middleShooterMotor.periodic();
            middleShooterMotor.setVelocityTarget(RPM.of(targetVelocity.get()));
        }

        if (rightShooterMotor != null) {
            rightShooterMotor.periodic();
            rightShooterMotor.setVelocityTarget(RPM.of(targetVelocity.get()));
        }
    }

    @Override
    public AngularVelocity getCurrentValue() {
        if (electricalContract.isMiddleShooterReady()) {
            return middleShooterMotor.getVelocity(); // TODO: Left/Right
        }
        return RPM.zero(); //rpm = rotation per minute
    }

    @Override
    public AngularVelocity getTargetValue() {
        return RPM.of(targetVelocity.get());
        //targetVelocity = double property - .get() makes it into a double
    }

    @Override
    public void setTargetValue(AngularVelocity value) {
        // TODO: Fill later
    }

    @Override
    public void setPower(Double power) {
        // TODO: Fill later
    }

    @Override
    public boolean isCalibrated() {
        return false;
    }

    @Override
    protected boolean areTwoTargetsEquivalent(AngularVelocity target1, AngularVelocity target2) {
        return target1.isEquivalent(target2);
    }
}