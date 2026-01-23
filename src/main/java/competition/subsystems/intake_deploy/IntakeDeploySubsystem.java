package competition.subsystems.intake_deploy;

import competition.electrical_contract.ElectricalContract;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class IntakeDeploySubsystem extends BaseSubsystem {
    public final XCANMotorController intakeDeployMotor;
    public DoubleProperty retrackPower;
    public DoubleProperty extendPower;

    @Inject
    public IntakeDeploySubsystem(XCANMotorController.XCANMotorControllerFactory xcanMotorControllerFactory,
              ElectricalContract electricalContract, XCANMotorController motor, PropertyFactory propertyFactory) {
        propertyFactory.setPrefix(this);
        if (electricalContract.isCollectDeployReady()) {
            this.intakeDeployMotor = xcanMotorControllerFactory.create(electricalContract.getCollectDeployMotor(),
                    getPrefix(),"collectorDeploy");
            this.registerDataFrameRefreshable(motor);
        } else {
            this.intakeDeployMotor = null;
        }

        this.retrackPower = propertyFactory.createPersistentProperty("retrackPower",-0.1);
        this.extendPower = propertyFactory.createPersistentProperty("extend Power",0.1);

    }
    public void intake() {
        intakeDeployMotor.setPower(retrackPower.get());
    }

    public void output() {
        intakeDeployMotor.setPower(extendPower.get());
    }

    public void stop() {
        intakeDeployMotor.setPower(0);
    }

    public void periodic() {
        intakeDeployMotor.periodic();
    }
}
