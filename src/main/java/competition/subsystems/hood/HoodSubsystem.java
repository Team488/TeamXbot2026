package competition.subsystems.hood;

import competition.electrical_contract.ElectricalContract;

import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.controls.actuators.XCANMotorControllerPIDProperties;
import xbot.common.controls.io_inputs.XCANCoderInputs;
import xbot.common.controls.sensors.XAbsoluteEncoder;
import xbot.common.controls.sensors.XCANCoder;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HoodSubsystem extends BaseSubsystem {
    public final XCANMotorController hoodMotor;
    public final XAbsoluteEncoder hoodEncoder;

    public DoubleProperty openPower;
    public DoubleProperty closePower;

    @Inject
    public HoodSubsystem(XCANMotorController.XCANMotorControllerFactory xcanMotorControllerFactory,
                         ElectricalContract electricalContract, PropertyFactory propertyFactory,
                         XAbsoluteEncoder.XAbsoluteEncoderFactory xAbsoluteEncoderFactory) {

        propertyFactory.setPrefix(this);

        if (electricalContract.isHoodReady()) {
            this.hoodMotor = xcanMotorControllerFactory.create(electricalContract.getHoodMotor(),
                    this.getPrefix(),
                    "HoodMotorPID",
                    new XCANMotorControllerPIDProperties());

        } else {
            this.hoodMotor = null;
        }

        if (electricalContract.isHoodAbsoluteEncoderReady()) {
            this.hoodEncoder = xAbsoluteEncoderFactory.create(electricalContract.getHoodAbsoluteEncoderMotor(),
                    getPrefix());
                    registerDataFrameRefreshable(hoodEncoder);
        } else {
            this.hoodEncoder = null;
        }

        openPower = propertyFactory.createPersistentProperty("open hood", 0.1);
        closePower = propertyFactory.createPersistentProperty("close hood", -0.1);
    }

    public void openHood() {
        if (hoodMotor != null) {
            hoodMotor.setPower(openPower.get());
        }
    }
    public void closeHood() {
        if (hoodMotor != null) {
            hoodMotor.setPower(openPower.get());
        }
    }
    public void stopHood() {
        if (hoodMotor != null) {
            hoodMotor.setPower(0);
        }
    }

}
