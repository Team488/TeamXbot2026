package competition.subsystems.intake_deploy;

import competition.electrical_contract.ElectricalContract;
import edu.wpi.first.units.measure.Angle;
import xbot.common.command.BaseSetpointSubsystem;

import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;
import xbot.common.controls.sensors.XAbsoluteEncoder;
import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public class IntakeDeploySubsystem extends BaseSetpointSubsystem<Angle,Double>  {
    public final XCANMotorController intakeDeployMotor;
    public final XAbsoluteEncoder intakeDeployAbsoluteEncoder;
    public DoubleProperty retractPower;
    public DoubleProperty extendPower;

    private Angle angleRotation;

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



    @Override
    public Angle getCurrentValue() {
        return intakeDeployAbsoluteEncoder.getAbsolutePosition();
    }


    @Override
    public Angle getTargetValue() {
        return angleRotation;
    }

    @Override
    public void setTargetValue(Angle value) {
        angleRotation = value;
    }

    @Override
    public void setPower(Double power) {
        intakeDeployMotor.setPower(power);
    }

    @Override
    public boolean isCalibrated() {
        return true;
    }

    @Override
    protected boolean areTwoTargetsEquivalent(Angle target1, Angle target2) {
        return target1.isEquivalent(target2);
    }
}