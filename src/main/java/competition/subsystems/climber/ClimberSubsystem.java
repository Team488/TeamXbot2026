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

    public final XCANMotorController climberMotorLeft;
    public final XCANMotorController climberMotorRight;
    public final XAbsoluteEncoder climberEncoder;
    private final DoubleProperty degreesPerRotation;
    public DoubleProperty climberPower;
    public double extendPower;
    public double retractPower;
    public ClimberState climberState;

    double encoderZeroOffset = 0;

    private boolean isCalibrated;

    private final MutAngle targetAngle = Degrees.mutable(0);

    public enum ClimberState {
        EXTENDING,
        RETRACTING,
        STOPPED
    }

    @Inject
    public ClimberSubsystem(XCANMotorController.XCANMotorControllerFactory motorFactory,
                            ElectricalContract electricalContract, PropertyFactory propertyFactory,
                            XAbsoluteEncoder.XAbsoluteEncoderFactory absoluteEncoder) {
        propertyFactory.setPrefix(this);

        if (electricalContract.isClimberLeftReady()) {
            this.climberMotorLeft = motorFactory.create(
                    electricalContract.getClimberMotorLeft(), this.getPrefix(), "ClimberMotorPID",
                    new XCANMotorControllerPIDProperties());
            this.registerDataFrameRefreshable(climberMotorLeft);
        } else {
            this.climberMotorLeft = null;
        }

        if (electricalContract.isClimberRightReady()) {
            this.climberMotorRight = motorFactory.create(
                    electricalContract.getClimberMotorRight(), this.getPrefix(), "ClimberMotorPID",
                    new XCANMotorControllerPIDProperties());
            this.registerDataFrameRefreshable(climberMotorRight);
        } else {
            this.climberMotorRight = null;
        }

        if (electricalContract.isClimberAbsoluteEncoderReady()) {
            this.climberEncoder = absoluteEncoder.create(
                    electricalContract.getClimberAbsoluteEncoder(),
                    getPrefix());
            this.registerDataFrameRefreshable(climberEncoder);
        } else {
            this.climberEncoder = null;
        }
        degreesPerRotation = propertyFactory.createPersistentProperty("Degrees Per Rotation", 0);
        // TODO: find degrees per rotation
    }

    public void extend() {
        if (climberMotorLeft != null) {
            climberMotorLeft.setPower(extendPower);
        }
        if (climberMotorRight != null) {
            climberMotorRight.setPower(extendPower);
        }
        climberState = ClimberState.EXTENDING;
    }

    public void retract() {
        if (climberMotorLeft != null) {
            climberMotorLeft.setPower(retractPower);
        }
        if (climberMotorRight != null) {
            climberMotorRight.setPower(retractPower);
        }
        climberState = ClimberState.RETRACTING;
    }

    public void stop() {
        if (climberMotorLeft != null) {
            climberMotorLeft.setPower(0);
        }
        if (climberMotorRight != null) {
            climberMotorRight.setPower(0);
        }
        climberState = ClimberState.STOPPED;
    }

    public void periodic() {
        if (climberMotorLeft != null) {
            climberMotorLeft.periodic();
        }
        if (climberMotorRight != null) {
            climberMotorRight.periodic();
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
       targetAngle.mut_replace(angle);
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
        return Math.abs(target1.in(Rotations)-target2.in(Rotations)) < .01;
    }

    private Angle getAbsoluteAngle() {
        if (climberMotorLeft != null) {
            return climberEncoder.getPosition();
        }
        return Degree.zero();
    }

    private void forceCalibration() {
        if (climberEncoder != null && climberEncoder.getAbsolutePosition() != null) {
            encoderZeroOffset = climberEncoder.getAbsolutePosition().in(Rotations);
            isCalibrated = true;
        }
    }

}