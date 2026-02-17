package competition.subsystems.shooter;

import competition.electrical_contract.ElectricalContract;
import edu.wpi.first.units.measure.AngularVelocity;
import xbot.common.command.BaseSetpointSubsystem;
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
public class ShooterSubsystem extends BaseSetpointSubsystem<AngularVelocity, Double> {
    public final XCANMotorController leftShooterMotor;
    public final XCANMotorController middleShooterMotor;
    public final XCANMotorController rightShooterMotor;
    public ElectricalContract electricalContract;

    public DoubleProperty targetVelocity;
    public DoubleProperty trimValue;

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
        this.trimValue = propertyFactory.createPersistentProperty("Shooter Trim Value", 0);
    }

    public void stop() {
        for (var motor : getShooterMotors()) {
            motor.setPower(0);
        }
    }

    public void increaseShooterOffset() {
        trimValue.set(trimValue.get() + 15);
    }

    public void decreaseShooterOffset() {
        trimValue.set(trimValue.get() - 15);
    }

    public void setTargetVelocity(double velocity) {
        targetVelocity.set(velocity);
    }

    public void runAtTargetVelocity() {
        for (var motor : getShooterMotors()) {
            motor.setVelocityTarget(RPM.of(targetVelocity.get()));
        }
    }

    public List<XCANMotorController> getShooterMotors() {
        var motors = new ArrayList<XCANMotorController>(3);
        if (leftShooterMotor != null) {
            motors.add(leftShooterMotor);
        }

        if (middleShooterMotor != null) {
            motors.add(middleShooterMotor);
        }

        if (rightShooterMotor != null) {
            motors.add(rightShooterMotor);
        }
        return motors;
    }

    public void periodic() {
        for (var motor : getShooterMotors()) {
            motor.periodic();
        }
    }


    @Override
    public AngularVelocity getCurrentValue() {
        // This avoids division by zero.
        if (getShooterMotors().isEmpty()) {
            return RPM.zero();
        }

        double total = 0;
        for (var motor : getShooterMotors()) {
            total += motor.getVelocity().in(RPM);
        }
        double averageSpeed = total / getShooterMotors().size();
        return RPM.of(averageSpeed);
    }

    @Override
    public AngularVelocity getTargetValue() {
        return RPM.of(targetVelocity.get());
    }


    @Override
    public void setTargetValue(AngularVelocity value) {
        targetVelocity.set(value.in(RPM));
    }

    @Override
    public void setPower(Double power) {
        for (var motor : getShooterMotors()) {
            motor.setPower(power);
        }
    }

    @Override
    public boolean isCalibrated() {
        return true;
    }

    @Override
    protected boolean areTwoTargetsEquivalent(AngularVelocity target1, AngularVelocity target2) {
        return Math.abs(target1.in(RPM) - target2.in(RPM)) < 0.00001;
    }
}