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
    public DoubleProperty retractPower;
    public DoubleProperty extendPower;

    @Inject
    public IntakeDeploySubsystem(XCANMotorController.XCANMotorControllerFactory xcanMotorControllerFactory,
              ElectricalContract electricalContract, XCANMotorController motor, PropertyFactory propertyFactory) {
        propertyFactory.setPrefix(this);
        if (electricalContract.isIntakeDeployReady()) {
            this.intakeDeployMotor = xcanMotorControllerFactory.create(electricalContract.getIntakeDeployMotor(),
                    getPrefix(),"collectorDeploy");
            this.registerDataFrameRefreshable(motor);
        } else {
            this.intakeDeployMotor = null;
        }

        this.retractPower = propertyFactory.createPersistentProperty("retrackPower",-0.1);
        this.extendPower = propertyFactory.createPersistentProperty("extend Power",0.1);

    }
    public void retract() {
        intakeDeployMotor.setPower(retractPower.get());
    }

    public void extend() {
        intakeDeployMotor.setPower(extendPower.get());
    }

    public void stop() {
        intakeDeployMotor.setPower(0);
    }

    public void periodic() {
        intakeDeployMotor.periodic();
    }
}
