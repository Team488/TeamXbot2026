package competition.subsystems.climber;

import competition.electrical_contract.ElectricalContract;
import edu.wpi.first.units.measure.Angle;
import org.dyn4j.geometry.Rotation;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.controls.actuators.XCANMotorControllerPIDProperties;
import xbot.common.controls.sensors.XAbsoluteEncoder;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.RPM;
import static edu.wpi.first.units.Units.Rotations;

@Singleton
public class ClimberSubsystem extends BaseSetpointSubsystem <Angle, Double> {

    public final XCANMotorController climberMotor;
    public final XAbsoluteEncoder climberEncoder;

    public final DoubleProperty extendPower;
    public final DoubleProperty retractPower;
    private final DoubleProperty degreesPerRotation;
    public DoubleProperty climberPower;

    double encoderZeroOffset = 0;

    private boolean isCalibrated;

    public Angle startingAngle;
    public Angle targetAngle;

    ElectricalContract el;

    @Inject
    public ClimberSubsystem(XCANMotorController.XCANMotorControllerFactory motorFactory,
                            ElectricalContract electricalContract, PropertyFactory propertyFactory,
                            XAbsoluteEncoder.XAbsoluteEncoderFactory absoluteEncoder) {
        propertyFactory.setPrefix(this);

        this.el = electricalContract;

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
            this.registerDataFrameRefreshable(climberEncoder);
        } else {
            this.climberEncoder = null;
        }

        extendPower = propertyFactory.createPersistentProperty("Extend Power", 0.1);
        retractPower = propertyFactory.createPersistentProperty("Retract Power", -0.1);
        degreesPerRotation = propertyFactory.createPersistentProperty("Degrees Per Rotation", 0); // TODO: find degrees per rotation
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
        if (climberMotor != null) {
            climberMotor.setPower(0);
        }
    }

    public void periodic() {
        if (climberMotor != null) {
            climberMotor.periodic();
        }

        if (!isCalibrated) {
            forceCalibration();
        }
    }


    @Override
    public Angle getCurrentValue() {
        double currentAngle = 0;
        if (el.isClimberAbsoluteEncoderReady()) {
            currentAngle = getCalibratedPosition().in(Rotations) * degreesPerRotation.get();
        }
        return Degrees.of(currentAngle);
    }

    @Override
    public Angle getTargetValue() {
        return targetAngle;
    }

    @Override
    public void setTargetValue(Angle angle) {

    }

    @Override
    public void setPower(Double aDouble) {

    }

    private Angle getCalibratedPosition() {
        return getAbsoluteAngle().minus(Rotations.of(encoderZeroOffset));
    }



    private Angle getMotorPosition() {
        if (el.isClimberReady()) {
            return climberEncoder.getPosition();
        }
        return startingAngle;
    }

    @Override
    public boolean isCalibrated() {
        return true;
    }

    @Override
    protected boolean areTwoTargetsEquivalent(Angle angle, Angle targetT1) {
        return false;
    }


    private Angle getAbsoluteAngle() {
        Angle currentAngle = Degrees.of(0);
        if (el.isClimberReady()) {
            currentAngle = climberEncoder.getPosition();
        }
        return currentAngle;
    }

    private void forceCalibration(){
        if (el.isClimberAbsoluteEncoderReady()) {
            encoderZeroOffset = climberEncoder.getAbsolutePosition().in(Rotations);
            isCalibrated = true;
        }
    }

}