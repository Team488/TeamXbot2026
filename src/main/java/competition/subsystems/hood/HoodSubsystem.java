package competition.subsystems.hood;

import competition.electrical_contract.ElectricalContract;

import competition.subsystems.hood.commands.HoodToGoalCommand;
import edu.wpi.first.units.measure.Time;
import edu.wpi.first.wpilibj.XboxController;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.TimedAndBoundedServo;
import xbot.common.controls.actuators.XServo;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.Optional;

import static edu.wpi.first.units.Units.Seconds;

@Singleton
public class HoodSubsystem extends BaseSubsystem {
    // Constants
    public static final double servoMinBound = 0.2;
    public static final double servoMaxBound = 0.8;
    public static final Time servoMinToMaxTime = Seconds.of(3);

    public final TimedAndBoundedServo hoodServoLeft;
    public final TimedAndBoundedServo hoodServoRight;
    public ElectricalContract electricalContract;

    public DoubleProperty servoTargetNormalized;
    public DoubleProperty trimValue;
    public DoubleProperty trimStep;
    public XboxController xboxController;
    public DoubleProperty extend;
    public DoubleProperty retract;

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
        this.trimValue = propertyFactory.createPersistentProperty("HoodTrimValue", 0);
        this.trimStep = propertyFactory.createPersistentProperty("HoodTrimStep", 0.1);
        this.extend = propertyFactory.createPersistentProperty("MinExtendGoal", 0);
        this.retract = propertyFactory.createPersistentProperty("MaxRetractGoal", 0.1);
    }

    public void extend() {
        if (hoodServoRight != null && hoodServoLeft != null) {
            double newHoodGoal;
            newHoodGoal = servoTargetNormalized.get() + trimStep.get();

            if (newHoodGoal > 1.0) {
                newHoodGoal = 1.0;
            }
            servoTargetNormalized.set(newHoodGoal);
            hoodServoLeft.setNormalizedTargetPosition(newHoodGoal);
            hoodServoRight.setNormalizedTargetPosition(newHoodGoal);
        }
    }
    public void retract() {
        if (hoodServoRight != null && hoodServoLeft != null) {
            double newHoodGoal;
            newHoodGoal = servoTargetNormalized.get() - trimStep.get();

            if (newHoodGoal < 0.0) {
                newHoodGoal = 0.0;
            }
            servoTargetNormalized.set(newHoodGoal);
            hoodServoLeft.setNormalizedTargetPosition(newHoodGoal);
            hoodServoRight.setNormalizedTargetPosition(newHoodGoal);
        }
    }

    public void stop() {
        if (hoodServoRight != null && hoodServoLeft != null) {
            double currentPosition = hoodServoLeft.getNormalizedCurrentPosition();
            servoTargetNormalized.set(currentPosition);
            hoodServoRight.setNormalizedTargetPosition(currentPosition);
            hoodServoLeft.setNormalizedTargetPosition(currentPosition);
        }
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

    public void trimHoodGoalUp() {
        trimValue.set(trimValue.get() + trimStep.get());
    }

    public void trimHoodGoalDown() {
        trimValue.set(trimValue.get() - trimStep.get());
    }

    @Override
    public void periodic() {
        if (this.hoodServoLeft != null && this.hoodServoRight != null) {
            aKitLog.record("LeftServoPosition", hoodServoLeft.getNormalizedCurrentPosition());
            aKitLog.record("RightServoPosition", hoodServoRight.getNormalizedCurrentPosition());
        }
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
}
