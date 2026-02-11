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
    public DoubleProperty servoDistancePercent;

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

        registerDataFrameRefreshable(hoodServoLeft);
        registerDataFrameRefreshable(hoodServoRight);

        this.servoMax = propertyFactory.createPersistentProperty("Servo Max", 0.8);
        this.servoMin = propertyFactory.createPersistentProperty("Servo Min", 0.2);
        this.servoDistancePercent = propertyFactory.createPersistentProperty("Servo Distance Percent", 1);
    }

    public void runServo() {
        hoodServoLeft.set(((servoMax.get() - servoMin.get()) * servoDistancePercent.get()) + servoMin.get());
    }

    public void servoZero() {
        hoodServoLeft.set(servoMin.get());
    }
}
