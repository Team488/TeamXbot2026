package competition.subsystems.intake_deploy;

import competition.electrical_contract.ElectricalContract;
import xbot.common.command.BaseSetpointSubsystem;

import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;
import xbot.common.controls.sensors.XAbsoluteEncoder;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class IntakeDeploySubsystem extends BaseSetpointSubsystem<Double,Double>  {
    public final XCANMotorController intakeDeployMotor;
    public final XAbsoluteEncoder intakeDeployAbsoluteEncoder;
    public DoubleProperty retractPower;
    public DoubleProperty extendPower;

    @Inject
    public IntakeDeploySubsystem(XCANMotorController.XCANMotorControllerFactory xcanMotorControllerFactory,
              ElectricalContract electricalContract, PropertyFactory propertyFactory,
                                 XAbsoluteEncoder.XAbsoluteEncoderFactory xAbsoluteEncoderFactory) {
        propertyFactory.setPrefix(this);
        if (electricalContract.isIntakeDeployReady()) {
            this.intakeDeployMotor = xcanMotorControllerFactory.create(electricalContract.getIntakeDeployMotor(),
                    getPrefix(),"intakeDeploy");
            this.registerDataFrameRefreshable(this.intakeDeployMotor);
        } else {
            this.intakeDeployMotor = null;
        }

        if (electricalContract.isIntakeDeployAbsoluteEncoderReady()) {
            this.intakeDeployAbsoluteEncoder = xAbsoluteEncoderFactory.create(electricalContract.getIntakeDeployAbsoluteEncoderMotor(),
                    getPrefix());
            registerDataFrameRefreshable(intakeDeployAbsoluteEncoder);
        } else {
            this.intakeDeployAbsoluteEncoder = null;
        }


        this.retractPower = propertyFactory.createPersistentProperty("retractPower", -0.1);
        this.extendPower = propertyFactory.createPersistentProperty("extendPower", 0.1);
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
        if (intakeDeployMotor != null) {
            intakeDeployMotor.periodic();
        }
    }

    @Override
    public Double getCurrentValue() {
        return null;
    }

    @Override
    public Double getTargetValue() {
        return null;
    }

    @Override
    public void setTargetValue(Double value) {

    }

    @Override
    public void setPower(Double power) {

    }

    @Override
    public boolean isCalibrated() {
        return false;
    }

    @Override
    protected boolean areTwoTargetsEquivalent(Double target1, Double target2) {
        return false;
    }
}
