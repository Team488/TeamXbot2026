package competition.subsystems.climber;

import competition.electrical_contract.ElectricalContract;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.controls.actuators.XCANMotorControllerPIDProperties;
import xbot.common.controls.sensors.XAbsoluteEncoder;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ClimberSubsystem extends BaseSubsystem {

    public final XCANMotorController climberMotor;
    public final XAbsoluteEncoder climberEncoder;

    public DoubleProperty extendPower;
    public DoubleProperty retractPower;

    @Inject
    public ClimberSubsystem(XCANMotorController.XCANMotorControllerFactory motorFactory,
                            ElectricalContract electricalContract, PropertyFactory propertyFactory,
                            XAbsoluteEncoder.XAbsoluteEncoderFactory absoluteEncoder) {
        propertyFactory.setPrefix(this);

        if (electricalContract.isClimberReady()) {
            this.climberMotor = motorFactory.create(
                    electricalContract.getClimberMotor(), this.getPrefix(), "ClimberMotorPID",
                    new XCANMotorControllerPIDProperties());
            this.registerDataFrameRefreshable(climberMotor);
        } else {
            this.climberMotor = null;
        }
        if (electricalContract.isClimberAbsoluteEncoderReady()) {
            this.climberEncoder = absoluteEncoder.create(
                    electricalContract.getClimberAbsoluteEncoder(),
            getPrefix());
        } else {
            this.climberEncoder = null;
        }

        extendPower = propertyFactory.createPersistentProperty("Extend Power",0.1);
        retractPower = propertyFactory.createPersistentProperty("Retract Power", -0.1);
    }

    public void extend() {
        if (climberMotor != null) {
            climberMotor.setPower(extendPower.get());
        }
    }

    public void retract() {
        if (climberMotor != null) {
            climberMotor.setPower(retractPower.get());
        }
    }

    public void stop() {
        if( climberMotor != null) {
            climberMotor.setPower(0);
        }
    }

    public void periodic() {
        if (climberMotor != null) {
            climberMotor.periodic();
        }
    }

    }

