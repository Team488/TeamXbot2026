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
    public final XCANMotorController intakeMotorLeft;
    public final XCANMotorController intakeMotorRight;
    DoubleProperty intakePower;
    DoubleProperty ejectPower;

    @Inject
    public IntakeSubsystem(ElectricalContract electricalContract,
                           XCANMotorController.XCANMotorControllerFactory motorFactory,
                           PropertyFactory pf) {

        pf.setPrefix(this);
        this.electricalContract = electricalContract;
        if (electricalContract.isFuelIntakeMotorLeftReady()) {
            this.intakeMotorLeft = motorFactory.create(
                    electricalContract.getFuelIntakeMotorLeft(),
                    getPrefix(),
                    "FuelIntakePID"
            );
            this.registerDataFrameRefreshable(intakeMotorLeft);
        } else {
            this.intakeMotorLeft = null;
        }
        if (electricalContract.isFuelIntakeMotorRightReady()){
            this.intakeMotorRight = motorFactory.create(
                    electricalContract.getFuelIntakeMotorRight(),
                    getPrefix(),
                    "FuelIntakePID"
            );
            this.registerDataFrameRefreshable(intakeMotorRight);
        } else {
            this.intakeMotorRight = null;
        }
        intakePower = pf.createPersistentProperty("FuelIntakePower", 1);
        ejectPower = pf.createPersistentProperty("FuelEjectPower", -1);
    }

    public void intake() {
        if (intakeMotorLeft != null) {
            intakeMotorLeft.setPower(intakePower.get());
        }
        if (intakeMotorRight != null) {
            intakeMotorRight.setPower(intakePower.get());
        }
    }

    public void eject() {
        if (intakeMotorLeft != null) {
            intakeMotorLeft.setPower(ejectPower.get());
        }
        if (intakeMotorRight != null) {
            intakeMotorRight.setPower(ejectPower.get());
        }
    }

    public void stop() {
        if (intakeMotorLeft != null) {
            intakeMotorRight.setPower(0);
        }
        if (intakeMotorRight != null) {
            intakeMotorRight.setPower(0);
        }
    }

    @Override
    public void periodic() {
        if (electricalContract.isFuelIntakeMotorLeftReady()) {
            intakeMotorLeft.periodic();
        }
        if (electricalContract.isFuelIntakeMotorRightReady()){
            intakeMotorRight.periodic();
        }
    }
}
