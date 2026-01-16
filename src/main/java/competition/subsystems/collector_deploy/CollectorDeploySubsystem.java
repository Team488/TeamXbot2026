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
    public final XCANMotorController motor;
    public DoubleProperty intakePower;
    public DoubleProperty outputPower;


    @Inject
    public  CollectorDeploySubsystem (XCANMotorController.XCANMotorControllerFactory xcanMotorControllerFactory,
                                      ElectricalContract electricalContract, PropertyFactory propertyFactory) {
        this.intakePower = propertyFactory.createPersistentProperty("intake Power",.1);
        this.outputPower = propertyFactory.createPersistentProperty("output Power",.1);
    }
}
