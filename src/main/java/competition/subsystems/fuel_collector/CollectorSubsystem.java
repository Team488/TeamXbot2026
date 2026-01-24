package competition.subsystems.fuel_collector;

import competition.electrical_contract.ElectricalContract;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CollectorSubsystem extends BaseSubsystem {

    public enum IntakeState {
        INTAKING,
        EJECTING,
        STOPPED
    }

    public final ElectricalContract electricalContract;
    public final XCANMotorController collectorMotor;
    DoubleProperty intakePower;
    DoubleProperty ejectPower;

    @Inject
    public CollectorSubsystem(ElectricalContract electricalContract,
                              XCANMotorController.XCANMotorControllerFactory motorFactory,
                              PropertyFactory pf) {

        pf.setPrefix(this);
        this.electricalContract = electricalContract;
        if (electricalContract.isFuelCollectorMotorReady()) {
            this.collectorMotor = motorFactory.create(
                    electricalContract.getFuelCollectorMotor(),
                    getPrefix(),
                    "FuelCollectorPID"
            );
            this.registerDataFrameRefreshable(collectorMotor);
        } else {
            this.collectorMotor = null;
        }

        //set intake and eject values, eject is - and intake is +
        intakePower = pf.createPersistentProperty("CollectorIntakePower", 1);
        ejectPower = pf.createPersistentProperty("CollectorEjectPower", -1);
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
        collectorMotor.setPower(ejectPower.get());
    }

    public void stop() {
        if (collectorMotor == null) {
            return;
        }
        collectorMotor.setPower(0);
    }

    @Override
    public void periodic() {
        if (electricalContract.isFuelCollectorMotorReady()) {
            collectorMotor.periodic();
        }
    }
}
