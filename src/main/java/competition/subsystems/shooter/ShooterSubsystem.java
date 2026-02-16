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

    public DoubleProperty shootingTargetVelocity;
    public DoubleProperty trimValue;
    public DoubleProperty readinessTimeoutSeconds;

    public AngularVelocity currentTargetVelocity = RPM.of(0);

    private final Subsystem trimSetpointLock = new Subsystem() {
    };

    @Inject
    public ShooterSubsystem(XCANMotorController.XCANMotorControllerFactory xcanMotorControllerFactory,
                            ElectricalContract electricalContract, PropertyFactory propertyFactory) {

        propertyFactory.setPrefix(this);
        this.electricalContract = electricalContract;

        var defaultPIDProperties = new XCANMotorControllerPIDProperties.Builder()
                .withP(0.0)
                .withI(0.0)
                .withD(0.0)
                .withStaticFeedForward(0)
                .withVelocityFeedForward(0.1)
                .withMinPowerOutput(-1.0)
                .withMaxPowerOutput(1.0)
                .build();

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

        this.shootingTargetVelocity = propertyFactory.createPersistentProperty("Shooting Target Velocity", 3000);
        this.trimValue = propertyFactory.createPersistentProperty("Shooter Trim Value", 0);
        this.readinessTimeoutSeconds = propertyFactory.createPersistentProperty("Readiness Timeout Seconds", 2.0);
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
        for (var motor : getShooterMotors()) {
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

    public void periodic() {
        for (var motor : getShooterMotors()) {
            motor.periodic();
        }
    }

    @Override
    public AngularVelocity getCurrentValue() {
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
}