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
    public final XCANMotorController collectorDeploy;
    public DoubleProperty intakePower;
    public DoubleProperty outputPower;


    @Inject
    public CollectorDeploySubsystem (XCANMotorController.XCANMotorControllerFactory xcanMotorControllerFactory,
                                      ElectricalContract electricalContract, XCANMotorController motor, PropertyFactory propertyFactory) {
        propertyFactory.setPrefix(this);
        if(electricalContract.isCollectDeployReady()){

            this.collectorDeploy = xcanMotorControllerFactory.create(electricalContract.getCollectDeployMotor(),
                    getPrefix(),"collectorDeploy");

        } else {
            this.collectorDeploy = null;
        }

        this.intakePower = propertyFactory.createPersistentProperty("intake Power",.1);
        this.outputPower = propertyFactory.createPersistentProperty("output Power",.1);

    }
    public void intake() {
        collectorDeploy.setPower(intakePower.get());
    }
    public void output() {
        collectorDeploy.setPower(outputPower.get());
    }
    public void stop() {
        collectorDeploy.setPower(0);
    }
}
