package competition.subsystems.hood;

import competition.electrical_contract.ElectricalContract;

import edu.wpi.first.units.measure.Time;
import edu.wpi.first.wpilibj2.command.Command;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.command.SimpleWaitForMaintainerCommand;
import xbot.common.controls.actuators.TimedAndBoundedServo;
import xbot.common.controls.actuators.XServo;
import xbot.common.math.MathUtils;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.Optional;

import static edu.wpi.first.units.Units.Seconds;

@Singleton
public class HoodSubsystem extends BaseSetpointSubsystem<Double, Double> {

    public static double getMechanismAngle(double servoPosition) {
        return Math.acos(mechanismAngleMax - servoPosition * (mechanismAngleMax - mechanismAngleMin));
    }

    public static double getServoPosition(double ballReleaseAngle) {
        return Math.acos(mechanismAngleMax - ballReleaseAngle / (mechanismAngleMax - mechanismAngleMin));
    }

    public static final double mechanismAngleMax = 75.6;
    public static final double mechanismAngleMin = 41.6;
    // Constants
    public static final double servoMinBound = 0.2;
    public static final double servoMaxBound = 0.65;
    public static final Time servoMinToMaxTime = Seconds.of(3);

    public final TimedAndBoundedServo hoodServoLeft;
    public final TimedAndBoundedServo hoodServoRight;
    public final ElectricalContract electricalContract;

    public final DoubleProperty servoTargetNormalized;
    public final DoubleProperty extend;
    public final DoubleProperty retract;
    public final DoubleProperty readinessTimeoutSeconds;

    public final DoubleProperty minDistanceGoal;
    public final DoubleProperty medDistanceGoal;
    public final DoubleProperty maxDistanceGoal;

    @Inject
    public HoodSubsystem(XServo.XServoFactory servoFactory,
                         ElectricalContract electricalContract, PropertyFactory propertyFactory) {
        propertyFactory.setPrefix(this);
        this.electricalContract = electricalContract;

        if (electricalContract.isHoodServoLeftReady()) {
            var servoLeft = servoFactory.create(
                    electricalContract.getHoodServoLeft().channel, getName() + "/Servo");
            registerDataFrameRefreshable(servoLeft);
            hoodServoLeft = new TimedAndBoundedServo(
                    servoLeft,
                    servoMinBound,
                    servoMaxBound,
                    servoMinToMaxTime.in(Seconds));
        } else {
            hoodServoLeft = null;
        }

        if (electricalContract.isHoodServoRightReady()) {
            var servoRight = servoFactory.create(
                    electricalContract.getHoodServoRight().channel, getName() + "/Servo");
            registerDataFrameRefreshable(servoRight);
            hoodServoRight = new TimedAndBoundedServo(
                    servoRight,
                    servoMinBound,
                    servoMaxBound,
                    servoMinToMaxTime.in(Seconds));
        } else {
            hoodServoRight = null;
        }

        this.servoTargetNormalized = propertyFactory.createPersistentProperty(
                "ServoTargetPositionNormalized", 0);
        this.extend = propertyFactory.createPersistentProperty("MaxExtensionGoal", 1.0);
        this.retract = propertyFactory.createPersistentProperty("MinExtensionGoal", 0.0);
        this.readinessTimeoutSeconds = propertyFactory.createPersistentProperty("ReadinessTimeoutSeconds", 3.0);

        this.minDistanceGoal = propertyFactory.createPersistentProperty("Hood Min Distance Goal", 0.0);
        this.medDistanceGoal = propertyFactory.createPersistentProperty("Hood Med Distance Goal", 0.5); //change this later
        this.maxDistanceGoal = propertyFactory.createPersistentProperty("Hood Max Distance Goal", 1.0); //change this later
    }

    public void extend() {
        setTargetValue(getTargetValue());
    }

    public void retract() {
        setTargetValue(getTargetValue());
    }

    public boolean isHoodDown() {
        return hoodServoLeft.getNormalizedCurrentPosition() <= 0.05;
    }

    public void runServo() {
        if (hoodServoLeft != null && hoodServoRight != null) {
            hoodServoLeft.setNormalizedTargetPosition(servoTargetNormalized.get());
            hoodServoRight.setNormalizedTargetPosition(servoTargetNormalized.get());
        }
    }

    public void servoZero() {
        servoTargetNormalized.set(0);
    }

    @Override
    public void periodic() {
        if (this.hoodServoLeft != null) {
            aKitLog.record("LeftServoPosition", hoodServoLeft.getNormalizedCurrentPosition());
        }

        if (this.hoodServoRight != null) {
            aKitLog.record("RightServoPosition", hoodServoRight.getNormalizedCurrentPosition());
        }
        aKitLog.record("HoodTargetPosition", getTargetValue());
    }

    public Optional<TimedAndBoundedServo> getHoodServoLeft() {
        if (hoodServoLeft == null) {
            return Optional.empty();
        } else {
            return Optional.of(hoodServoLeft);
        }
    }

    public Optional<TimedAndBoundedServo> getHoodServoRight() {
        if (hoodServoRight == null) {
            return Optional.empty();
        } else {
            return Optional.of(hoodServoRight);
        }
    }

    @Override
    public Double getCurrentValue() {
        if(hoodServoLeft == null) { return 0.0; }
        return hoodServoLeft.getNormalizedCurrentPosition();
    }

    @Override
    public Double getTargetValue() {
        double minPosition = retract.get();
        double maxPosition = extend.get();
        double targetRatio = servoTargetNormalized.get();
        return MathUtils.constrainDouble(targetRatio, minPosition, maxPosition);
    }

    @Override
    public void setTargetValue(Double targetRatio) {
        double minPosition = retract.get();
        double maxPosition = extend.get();
        targetRatio = MathUtils.constrainDouble(targetRatio, minPosition, maxPosition);
        servoTargetNormalized.set(targetRatio);
    }

    @Override
    public void setPower(Double power) {}

    @Override
    public boolean isCalibrated() {
        // Since this subsystem uses servos with no feedback, we can
        // consider it always calibrated.
        return true;
    }

    @Override
    protected boolean areTwoTargetsEquivalent(Double target1, Double target2) {
        return Math.abs(target1 - target2) < 0.1;
    }

    public Command getWaitForAtGoalCommand() {
        return new SimpleWaitForMaintainerCommand(this, readinessTimeoutSeconds::get);
    }
}
