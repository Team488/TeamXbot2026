package competition.subsystems.collector_intake;

import competition.electrical_contract.ElectricalContract;
import competition.electrical_contract.Hardware;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.controls.actuators.XCANMotorControllerPIDProperties;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CollectorSubsystem extends BaseSubsystem {

    public final ElectricalContract electricalContract;
    public final XCANMotorController collectorMotor;

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

        if (electricalContract.isReady(Hardware.FuelIntake)) {
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
    }

    // TODO: add methods to intake, eject, and stop the collector

    @Override
    public void periodic() {
        if (collectorMotor != null) {
            collectorMotor.periodic();
        }
    }
}
