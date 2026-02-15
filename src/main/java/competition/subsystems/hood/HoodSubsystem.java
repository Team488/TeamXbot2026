package competition.subsystems.hood;

import competition.electrical_contract.ElectricalContract;

import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XServo;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HoodSubsystem extends BaseSubsystem {
    public final XServo hoodServoLeft;
    public final XServo hoodServoRight;
    public ElectricalContract electricalContract;

    public DoubleProperty servoMax;
    public DoubleProperty servoMin;
    public DoubleProperty servoDistanceRatio;
    public DoubleProperty trimValue;

    @Inject
    public HoodSubsystem(XServo.XServoFactory servoFactory,
                         ElectricalContract electricalContract, PropertyFactory propertyFactory) {

        propertyFactory.setPrefix(this);
        this.electricalContract = electricalContract;

        if (electricalContract.isHoodServoLeftReady()) {
           this.hoodServoLeft = servoFactory.create(electricalContract.getHoodServoLeft().channel,
                   getName() + "/Servo");
        } else {
            this.hoodServoLeft = null;
        }

        if (electricalContract.isHoodServoRightReady()) {
            this.hoodServoRight = servoFactory.create(electricalContract.getHoodServoRight().channel,
                    getName() + "/Servo");
        } else {
            this.hoodServoRight = null;
        }

        this.servoMax = propertyFactory.createPersistentProperty("Servo Max", 0.8);
        //3 and 14/16 inches away from base

        this.servoMin = propertyFactory.createPersistentProperty("Servo Min", 0.2);
        this.servoDistanceRatio = propertyFactory.createPersistentProperty("Servo Distance Ratio", 0);
        this.trimValue = propertyFactory.createPersistentProperty("Hood Trim Value", 0);
    }

    public void runServo() {
        if (hoodServoLeft != null && hoodServoRight != null) {
            double distanceGoal = ((servoMax.get() - servoMin.get()) * servoDistanceRatio.get()) + servoMin.get();
            hoodServoLeft.set(distanceGoal);
            hoodServoRight.set(distanceGoal);
        }
    }

    public void servoZero() {
        servoDistanceRatio.set(0);
    }

    public void trimHoodGoalUp() {
        trimValue.set(trimValue.get() + 0.005);
    }

    public void trimHoodGoalDown() {
        trimValue.set(trimValue.get() - 0.005);
    }
}
