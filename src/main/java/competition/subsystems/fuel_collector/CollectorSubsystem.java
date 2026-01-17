package competition.subsystems.fuel_collector;

import competition.electrical_contract.ElectricalContract;
import competition.operator_interface.OperatorInterface;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.injection.electrical_contract.CANMotorControllerInfo;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

public class CollectorSubsystem extends BaseSubsystem {

    public enum IntakeState {
        INTAKING,
        EJECTING,
        STOPPED
    }

    public final XCANMotorController collectorMotor;
    DoubleProperty intakePower;
    DoubleProperty outputPower;

    public CollectorSubsystem(ElectricalContract electricalContract,
                              XCANMotorController.XCANMotorControllerFactory motorFactory,
                              PropertyFactory pf) {

        pf.setPrefix(this);
        if (electricalContract.isgetFuelCollectorMotorReady()) {
            this.collectorMotor = motorFactory.create(
                    electricalContract.getFuelCollectorMotor(),
                    getPrefix(),
                    "fuelCollector"
            );
        } else {
            this.collectorMotor = null;
        }
        //set intake and output values, output should be - and intake is +

        intakePower = pf.createPersistentProperty("intake Power", 1);
        outputPower = pf.createPersistentProperty("output Power", -1);

    }


    public void intake() {
        if (collectorMotor == null) {
            return;
        }
        collectorMotor.setPower(intakePower.get());
    }

    public void eject() {
        if (collectorMotor == null) {
            return;
        }
        collectorMotor.setPower(outputPower.get());
    }

    public void stop() {
        if (collectorMotor == null) {
            return;
        }
        collectorMotor.setPower(0);
    }
}
