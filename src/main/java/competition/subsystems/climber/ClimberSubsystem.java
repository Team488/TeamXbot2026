package competition.subsystems.climber;

import competition.electrical_contract.ElectricalContract;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.MutAngle;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.controls.actuators.XCANMotorControllerPIDProperties;
import xbot.common.controls.sensors.XAbsoluteEncoder;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.Degree;
import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.Rotations;

@Singleton
public class ClimberSubsystem extends BaseSetpointSubsystem <Angle, Double> {

    public final XCANMotorController climberMotor;
    public final XAbsoluteEncoder climberEncoder;
    private final DoubleProperty degreesPerRotation;
    public DoubleProperty climberPower;

    double encoderZeroOffset = 0;

    private boolean isCalibrated;

    private final MutAngle targetAngle = Degrees.mutable(0);



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
            this.registerDataFrameRefreshable(climberEncoder);
        } else {
            this.climberEncoder = null;
        }

        degreesPerRotation = propertyFactory.createPersistentProperty("Degrees Per Rotation", 0); // TODO: find degrees per rotation
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
        if (climberEncoder != null) {
            currentAngle = getCalibratedPosition().in(Rotations) * degreesPerRotation.get();
        }
        return Degrees.of(currentAngle);
    }

    @Override
    public Angle getTargetValue() {
        return targetAngle.copy();
    }

    @Override
    public void setTargetValue(Angle angle) {

    }

    @Override
    public void setPower(Double power) {

    }

    private Angle getCalibratedPosition() {
        return getAbsoluteAngle().minus(Rotations.of(encoderZeroOffset));
    }

    @Override
    public boolean isCalibrated() {
        return isCalibrated;
    }

    @Override
    protected boolean areTwoTargetsEquivalent(Angle target1, Angle target2) {
        return target1.equals(target2);
    }

    private Angle getAbsoluteAngle() {
        if (climberMotor != null) {
            return climberEncoder.getPosition();
        }
        return Degree.zero();
    }

    private void forceCalibration() {
        if (climberEncoder != null) {
            encoderZeroOffset = climberEncoder.getAbsolutePosition().in(Rotations);
            isCalibrated = true;
        }
    }

}