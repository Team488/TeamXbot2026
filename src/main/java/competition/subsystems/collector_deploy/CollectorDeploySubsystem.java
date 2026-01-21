package competition.subsystems.collector_deploy;

import competition.electrical_contract.ElectricalContract;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CollectorDeploySubsystem extends BaseSubsystem {
    public final XCANMotorController collectorDeployMotor;
    public DoubleProperty retrackPower;
    public DoubleProperty extendPower;

    @Inject
    public CollectorDeploySubsystem (XCANMotorController.XCANMotorControllerFactory xcanMotorControllerFactory,
                                      ElectricalContract electricalContract, XCANMotorController motor, PropertyFactory propertyFactory) {
        propertyFactory.setPrefix(this);
        if (electricalContract.isCollectDeployReady()) {
            this.collectorDeployMotor = xcanMotorControllerFactory.create(electricalContract.getCollectDeployMotor(),
                    getPrefix(),"collectorDeploy");
            this.registerDataFrameRefreshable(motor);
        } else {
            this.collectorDeployMotor = null;
        }

        this.retrackPower = propertyFactory.createPersistentProperty("retrackPower",-0.1);
        this.extendPower = propertyFactory.createPersistentProperty("extend Power",0.1);

    }
    public void intake() {
        collectorDeployMotor.setPower(retrackPower.get());
    }

    public void output() {
        collectorDeployMotor.setPower(extendPower.get());
    }

    public void stop() {
        collectorDeployMotor.setPower(0);
    }

    public void periodic() {
        collectorDeployMotor.periodic();
    }
}
