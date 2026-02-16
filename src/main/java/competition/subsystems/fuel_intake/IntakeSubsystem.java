package competition.subsystems.fuel_intake;

import competition.electrical_contract.ElectricalContract;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class IntakeSubsystem extends BaseSubsystem {

    public enum IntakeState {
        INTAKING,
        EJECTING,
        STOPPED
    }

    public final ElectricalContract electricalContract;
    public final XCANMotorController intakeMotor;
    DoubleProperty intakePower;
    DoubleProperty ejectPower;

    @Inject
    public IntakeSubsystem(ElectricalContract electricalContract,
                           XCANMotorController.XCANMotorControllerFactory motorFactory,
                           PropertyFactory pf) {

        pf.setPrefix(this);
        this.electricalContract = electricalContract;
        if (electricalContract.isFuelIntakeMotorReady()) {
            this.intakeMotor = motorFactory.create(
                    electricalContract.getFuelIntakeMotor(),
                    getPrefix(),
                    "FuelIntakePID"
            );
            this.registerDataFrameRefreshable(intakeMotor);
        } else {
            this.intakeMotor = null;
        }

        intakePower = pf.createPersistentProperty("FuelIntakePower", 1);
        ejectPower = pf.createPersistentProperty("FuelEjectPower", -1);
    }

    public void intake() {
        if (intakeMotor == null) {
            return;
        }
        intakeMotor.setPower(intakePower.get());
    }

    public void eject() {
        if (intakeMotor == null) {
            return;
        }
        intakeMotor.setPower(ejectPower.get());
    }

    public void stop() {
        if (intakeMotor == null) {
            return;
        }
        intakeMotor.setPower(0);
    }

    @Override
    public void periodic() {
        if (electricalContract.isFuelIntakeMotorReady()) {
            intakeMotor.periodic();
        }
    }
}
