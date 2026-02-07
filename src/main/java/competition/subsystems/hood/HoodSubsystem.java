package competition.subsystems.hood;

import competition.electrical_contract.ElectricalContract;

import edu.wpi.first.units.measure.Angle;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.controls.actuators.XCANMotorControllerPIDProperties;
import xbot.common.controls.actuators.XServo;
import xbot.common.injection.electrical_contract.DeviceInfo;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.Rotations;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HoodSubsystem extends BaseSubsystem {
    public final XServo hoodServo;
    public ElectricalContract electricalContract;

    public DoubleProperty servoDistance;

    @Inject
    public HoodSubsystem(XServo.XServoFactory servoFactory,
                         ElectricalContract electricalContract, PropertyFactory propertyFactory) {

        propertyFactory.setPrefix(this);
        this.electricalContract = electricalContract;

        if (electricalContract.isHoodReady()) {
           this.hoodServo = servoFactory.create(electricalContract.getHoodServo().channel,
                   getName() + "/Servo");
        } else {
            this.hoodServo = null;
        }

        this.servoDistance = propertyFactory.createPersistentProperty("Servo Distance Goal", 1);
    }

    public void runServo() {
        hoodServo.set(servoDistance.get());
    }

    public void stopServo() {
        hoodServo.set(0);
    }
}
