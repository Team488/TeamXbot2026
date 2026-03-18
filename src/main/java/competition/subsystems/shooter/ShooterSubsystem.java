package competition.subsystems.shooter;

import competition.electrical_contract.ElectricalContract;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Subsystem;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.command.SimpleWaitForMaintainerCommand;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.controls.actuators.XCANMotorControllerPIDProperties;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;
import xbot.common.resiliency.DeviceHealth;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

import static edu.wpi.first.units.Units.RPM;
import static edu.wpi.first.units.Units.Rotations;
import static edu.wpi.first.units.Units.RotationsPerSecond;
import static edu.wpi.first.units.Units.Seconds;

@Singleton
public class ShooterSubsystem extends BaseSetpointSubsystem<AngularVelocity, Double> {
    public static boolean isReadyToFire;
    public final XCANMotorController leftShooterMotor;
    public final XCANMotorController middleShooterMotor;
    public final XCANMotorController rightShooterMotor;
    public ElectricalContract electricalContract;
    public PropertyFactory propertyFactory;

    public final DoubleProperty defaultShootingVelocity;
    public final DoubleProperty trimValue;
    public final DoubleProperty voltageRampTime;
    public DoubleProperty readinessTimeoutSeconds;
    boolean isInLowPowerMode = false;

    public AngularVelocity currentTargetVelocity = RPM.of(0);

    private final Subsystem trimSetpointLock = new Subsystem() {

    };

    @Inject
    public ShooterSubsystem(XCANMotorController.XCANMotorControllerFactory xcanMotorControllerFactory,
                            ElectricalContract electricalContract, PropertyFactory propertyFactory) {

        this.propertyFactory = propertyFactory;

        this.propertyFactory.setPrefix(this);
        this.electricalContract = electricalContract;

        var leftShooterMotorDefaultPIDProperties = new XCANMotorControllerPIDProperties.Builder()
                .withP(0.05)
                .withI(0.01)
                .withD(0.01)
                .withStaticFeedForward(0.02)
                .withVelocityFeedForward(0.015)
                .withMinPowerOutput(-1.0)
                .withMaxPowerOutput(1.0)
                .build();

        var middleShooterMotorDefaultPIDProperties = new XCANMotorControllerPIDProperties.Builder()
                .withP(0.05)
                .withI(0.01)
                .withD(0.01)
                .withStaticFeedForward(0.02)
                .withVelocityFeedForward(0.016)
                .withMinPowerOutput(-1.0)
                .withMaxPowerOutput(1.0)
                .build();

        var rightShooterMotorDefaultPIDProperties = new XCANMotorControllerPIDProperties.Builder()
                .withP(0.05)
                .withI(0.01)
                .withD(0.01)
                .withStaticFeedForward(0.02)
                .withVelocityFeedForward(0.016)
                .withMinPowerOutput(-1.0)
                .withMaxPowerOutput(1.0)
                .build();

        this.voltageRampTime = propertyFactory.createPersistentProperty("VoltageRampTime", 0.2);

        if (electricalContract.isLeftShooterReady()) {
            this.leftShooterMotor = xcanMotorControllerFactory.create(electricalContract.getLeftShooterMotor(),
                    getPrefix(), "leftShooterMotor", leftShooterMotorDefaultPIDProperties);
            this.leftShooterMotor.setClosedLoopRampRates(
                    Seconds.of(voltageRampTime.get()),
                    Seconds.of(voltageRampTime.get()));
            this.registerDataFrameRefreshable(leftShooterMotor);
        } else {
            this.leftShooterMotor = null;
        }

        if (electricalContract.isMiddleShooterReady()) {
            this.middleShooterMotor = xcanMotorControllerFactory.create(electricalContract.getMiddleShooterMotor(),
                    getPrefix(), "middleShooterMotor", middleShooterMotorDefaultPIDProperties);
            this.middleShooterMotor.setClosedLoopRampRates(
                    Seconds.of(voltageRampTime.get()),
                    Seconds.of(voltageRampTime.get()));
            this.registerDataFrameRefreshable(middleShooterMotor);
        } else {
            this.middleShooterMotor = null;
        }

        if (electricalContract.isRightShooterReady()) {
            this.rightShooterMotor = xcanMotorControllerFactory.create(electricalContract.getRightShooterMotor(),
                    getPrefix(), "rightShooterMotor", rightShooterMotorDefaultPIDProperties);
            this.rightShooterMotor.setClosedLoopRampRates(
                    Seconds.of(voltageRampTime.get()),
                    Seconds.of(voltageRampTime.get()));
            this.registerDataFrameRefreshable(rightShooterMotor);
        } else {
            this.rightShooterMotor = null;
        }

        this.defaultShootingVelocity = this.propertyFactory.createPersistentProperty("Default Shooter Velocity RPM", 3000);
        this.trimValue = this.propertyFactory.createPersistentProperty("Shooter Trim Value", 0);
        this.readinessTimeoutSeconds = this.propertyFactory.createPersistentProperty("Readiness Timeout Seconds", 2.0);
    }

    public void stop() {
        for (var motor : getShooterMotors()) {
            motor.setPower(0);
        }
    }

    public Subsystem getTrimSetpointLock() {
        return this.trimSetpointLock;
    }

    public void increaseShooterOffset() {
        trimValue.set(trimValue.get() + 15);
    }

    public void decreaseShooterOffset() {
        trimValue.set(trimValue.get() - 15);
    }

    public void runMotorsAtVelocity(AngularVelocity velocity) {
        for (var motor : getHealthyShooterMotors()) {
            motor.setVelocityTarget(velocity);
        }
    }

    public boolean isReadyToFire() {
        return isMaintainerAtGoal() && hasNonIdleTarget();
    }

    public boolean hasNonIdleTarget() {
        return currentTargetVelocity.gt(RPM.of(300));
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

    public List<XCANMotorController> getHealthyShooterMotors() {
        // Low power mode, pick one motor, prioritizing the middle motor,
        // but falling back to one of the others if middle is unhealthy.
        if (isInLowPowerMode
                && middleShooterMotor != null
                && middleShooterMotor.getHealth() == DeviceHealth.Healthy) {
            return List.of(middleShooterMotor);
        } else if (isInLowPowerMode
                && leftShooterMotor != null
                && leftShooterMotor.getHealth() == DeviceHealth.Healthy) {
            return List.of(leftShooterMotor);
        } else if (isInLowPowerMode
                && rightShooterMotor != null
                && rightShooterMotor.getHealth() == DeviceHealth.Healthy) {
            return List.of(rightShooterMotor);
        } else if (isInLowPowerMode) {
            return List.of();
        } else {
            // Not low power mode, run everything
            return getShooterMotors().stream()
                    .filter(motor -> motor.getHealth() == DeviceHealth.Healthy)
                    .toList();
        }
    }

    public void periodic() {
        if (voltageRampTime.hasChangedSinceLastCheck()) {
            for (var motor : getShooterMotors()) {
                motor.setClosedLoopRampRates(
                        Seconds.of(voltageRampTime.get()),
                        Seconds.of(voltageRampTime.get()));
            }
        }

        for (var motor : getShooterMotors()) {
            motor.periodic();
        }
        aKitLog.record("ShooterCurrentVelocity", getCurrentValue());
        aKitLog.record("ShooterTargetVelocity", getTargetValue());
        aKitLog.record("isCalibrated", isCalibrated());
        aKitLog.record("LowPowerMode", isInLowPowerMode);
    }

    @Override
    public AngularVelocity getCurrentValue() {
        var shooterMotors = getHealthyShooterMotors();
        if (shooterMotors.isEmpty()) {
            return RPM.zero();
        }

        double total = 0;
        for (var motor : shooterMotors) {
            total += motor.getVelocity().in(RPM);
        }
        double averageSpeed = total / shooterMotors.size();
        return RPM.of(averageSpeed);
    }

    @Override
    public AngularVelocity getTargetValue() {
        return currentTargetVelocity;
    }

    public AngularVelocity getTrimmedTargetValue() {
        if (currentTargetVelocity.isEquivalent(RPM.zero())) {
            return currentTargetVelocity;
        }
        return currentTargetVelocity.plus(RPM.of(trimValue.get()));
    }

    @Override
    public void setTargetValue(AngularVelocity value) {
        currentTargetVelocity = value;
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

    public Command getWaitForAtGoalCommand() {
        return new SimpleWaitForMaintainerCommand(this, () -> readinessTimeoutSeconds.get());
    }

    public void setLowPowerMode(boolean newValue) {
        this.isInLowPowerMode = newValue;
    }
}
