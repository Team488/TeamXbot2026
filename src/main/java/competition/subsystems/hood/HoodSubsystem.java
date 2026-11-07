package competition.subsystems.hood;

import competition.electrical_contract.ElectricalContract;
import competition.electrical_contract.Hardware;
import edu.wpi.first.units.measure.Time;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.controls.actuators.TimedAndBoundedServo;
import xbot.common.controls.actuators.XServo;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.Seconds;

/**
 * The hood is moved by two servos (left and right) that should always move together.
 * Servos have no position sensor, so TimedAndBoundedServo estimates the current position
 * based on how long the servo has been moving. Positions are "normalized" from 0 (retracted) to 1 (extended).
 */
@Singleton
public class HoodSubsystem extends BaseSetpointSubsystem<Double, Double> {

    // Angle of the hood (in degrees) at either end of its travel
    public static final double mechanismAngleMax = 75.6;
    public static final double mechanismAngleMin = 41.6;

    // Servo hardware limits
    public static final double servoMinBound = 0.2;
    public static final double servoMaxBound = 0.65;
    public static final Time servoMinToMaxTime = Seconds.of(3);

    public final TimedAndBoundedServo hoodServoLeft;
    public final TimedAndBoundedServo hoodServoRight;
    public final ElectricalContract electricalContract;

    @Inject
    public HoodSubsystem(XServo.XServoFactory servoFactory,
                         ElectricalContract electricalContract, PropertyFactory propertyFactory) {
        propertyFactory.setPrefix(this);
        this.electricalContract = electricalContract;

        if (electricalContract.isReady(Hardware.HoodServoLeft)) {
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

        if (electricalContract.isReady(Hardware.HoodServoRight)) {
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
    }

    @Override
    public Double getCurrentValue() {
        // TODO: return the current normalized (0 to 1) position of the hood
        return 0.0;
    }

    @Override
    public Double getTargetValue() {
        // TODO: return the target normalized (0 to 1) position of the hood
        return 0.0;
    }

    @Override
    public void setTargetValue(Double value) {
        // TODO: remember this target position in this class for later
    }

    @Override
    public void setPower(Double power) {
        // Servos don't take power, they take a target position. Nothing to do here.
    }

    @Override
    public boolean isCalibrated() {
        // TODO: servos have no feedback, do they need calibration?
        return false;
    }

    @Override
    protected boolean areTwoTargetsEquivalent(Double target1, Double target2) {
        return Math.abs(target1 - target2) < 0.1;
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
}
