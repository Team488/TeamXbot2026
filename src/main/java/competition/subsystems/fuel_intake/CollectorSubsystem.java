package competition.subsystems.fuel_intake;

import competition.electrical_contract.ElectricalContract;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CollectorSubsystem extends BaseSubsystem {

    public final ElectricalContract electricalContract;
    public final XCANMotorController collectorMotor;
    final DoubleProperty intakePower;
    final DoubleProperty ejectPower;

    @Inject
    public CollectorSubsystem(ElectricalContract electricalContract,
                              XCANMotorController.XCANMotorControllerFactory motorFactory,
                              PropertyFactory pf) {

        pf.setPrefix(this);
        this.electricalContract = electricalContract;
        if (electricalContract.isFuelIntakeMotorReady()) {
            this.collectorMotor = motorFactory.create(
                    electricalContract.getFuelIntakeMotor(),
                    getPrefix(),
                    "FuelIntakePID"
            );
            this.registerDataFrameRefreshable(collectorMotor);
        } else {
            this.collectorMotor = null;
        }

        intakePower = pf.createPersistentProperty("FuelIntakePower", 1);
        ejectPower = pf.createPersistentProperty("FuelEjectPower", -1);
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
        if (electricalContract.isFuelIntakeMotorReady()) {
            collectorMotor.periodic();
        }
    }
}
