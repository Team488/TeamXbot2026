package competition.subsystems.hood;

import competition.electrical_contract.ElectricalContract;

import edu.wpi.first.units.measure.Time;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.TimedAndBoundedServo;
import xbot.common.controls.actuators.XServo;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

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
        trimValue.set(trimValue.get() + 0.005);
    }

    public void trimHoodGoalDown() {
        trimValue.set(trimValue.get() - 0.005);
    }

    @Override
    public void periodic() {
        aKitLog.record("LeftServoPosition", hoodServoLeft.getNormalizedCurrentPosition());
        aKitLog.record("RightServoPosition", hoodServoRight.getNormalizedCurrentPosition());
    }

    public TimedAndBoundedServo getHoodServoLeft() {
        return hoodServoLeft;
    }

    public TimedAndBoundedServo getHoodServoRight() {
        return hoodServoRight;
    }
}
