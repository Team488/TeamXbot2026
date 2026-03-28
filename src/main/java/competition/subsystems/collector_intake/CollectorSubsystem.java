package competition.subsystems.collector_intake;

import competition.electrical_contract.ElectricalContract;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.controls.actuators.XCANMotorControllerPIDProperties;
import xbot.common.properties.AngularVelocityProperty;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.RPM;

@Singleton
public class CollectorSubsystem extends BaseSubsystem {

    public final ElectricalContract electricalContract;
    public final XCANMotorController collectorMotor;
    final AngularVelocityProperty intakeVelocity;
    final DoubleProperty intakePower;
    final DoubleProperty ejectPower;

    @Inject
    public CollectorSubsystem(ElectricalContract electricalContract,
                              XCANMotorController.XCANMotorControllerFactory motorFactory,
                              PropertyFactory pf) {

        pf.setPrefix(this);
        this.electricalContract = electricalContract;

        var collectorMotorDefaultPIDProperties = new XCANMotorControllerPIDProperties.Builder()
                .withP(0.05)
                .withI(0.0)
                .withD(0.0)
                .withStaticFeedForward(0.02)
                .withVelocityFeedForward(0.01)
                .withMinPowerOutput(-1.0)
                .withMaxPowerOutput(1.0)
                .build();

        if (electricalContract.isFuelIntakeMotorReady()) {
            this.collectorMotor = motorFactory.create(
                    electricalContract.getFuelIntakeMotor(),
                    getPrefix(),
                    "FuelIntakePID", 
                    collectorMotorDefaultPIDProperties
            );
            this.registerDataFrameRefreshable(collectorMotor);
        } else {
            this.collectorMotor = null;
        }

        intakeVelocity = pf.createPersistentProperty("FuelIntakePower", RPM.of(3000));
        intakePower = pf.createPersistentProperty("FuelIntakePower", 1);
        ejectPower = pf.createPersistentProperty("FuelEjectPower", -1);
    }

    public void intakeVelocity() {
        if (collectorMotor == null) {
            return;
        }
        collectorMotor.setVelocityTarget(intakeVelocity.get());
    }

    public void intakePower() {
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
