package competition.subsystems.climber;

import competition.electrical_contract.ElectricalContract;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.MutAngle;
import edu.wpi.first.wpilibj2.command.Command;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.command.NamedRunCommand;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.controls.actuators.XCANMotorControllerPIDProperties;
import xbot.common.controls.sensors.XAbsoluteEncoder;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.Degree;
import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.DegreesPerSecond;
import static edu.wpi.first.units.Units.Rotations;
import static edu.wpi.first.units.Units.RotationsPerSecond;

@Singleton
public class ClimberSubsystem extends BaseSetpointSubsystem <Angle, Double> {

    public final XCANMotorController climberMotorLeft;
    public final XCANMotorController climberMotorRight;
    public final XAbsoluteEncoder climberEncoder;
    private final DoubleProperty degreesPerRotation;
    public final DoubleProperty manualControlPower;
    public DoubleProperty extendPower;
    public DoubleProperty retractPower;
    public ClimberState climberState;
    Angle encoderZeroOffset = Degrees.zero();

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
        if (electricalContract.isClimberLeftReady() && electricalContract.isClimberRightReady()) {
            this.climberMotorLeft = motorFactory.create(
                    electricalContract.getClimberMotorLeft(),
                    getPrefix(), "ClimberMotorPID", new XCANMotorControllerPIDProperties(
                            0,
                            0,
                            0
                    ));
            this.climberMotorRight = motorFactory.create(
                    electricalContract.getClimberMotorRight(),
                    getPrefix(), "ClimberMotorPID", new XCANMotorControllerPIDProperties(
                            0,
                            0,
                            0
                    ));
            this.registerDataFrameRefreshable(climberMotorLeft);
            this.registerDataFrameRefreshable(climberMotorRight);
        } else {
            this.climberMotorLeft = null;
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
        this.manualControlPower = propertyFactory.createPersistentProperty("ManualControlPower", 0.1);
        // TODO: find degrees per rotation
    }
        //set target position for rotation
    public void extend() {
        if (climberMotorLeft != null) {
            climberMotorLeft.setPower(extendPower.get());
        }
        if (climberMotorRight != null) {
            climberMotorRight.setPower(extendPower.get());
        }
        climberState = ClimberState.EXTENDING;
    }

    public void retract() {
        if (climberMotorLeft != null) {
            climberMotorLeft.setPower(retractPower.get());
        }
        if (climberMotorRight != null) {
            climberMotorRight.setPower(retractPower.get());
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
        return getAbsoluteAngle().minus(encoderZeroOffset);
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

    public void calibrateOffsetRetracted() {
        if (climberMotorLeft != null) {
            encoderZeroOffset = climberMotorLeft.getPosition();
            isCalibrated = true;
        }
    }

    public void setPositionalGoalIncludingOffset(Angle setpoint) {
        climberMotorRight.setPositionTarget(
                Rotations.of(setpoint.in(Degrees) / degreesPerRotation.get()).plus(encoderZeroOffset),
                XCANMotorController.MotorPidMode.Voltage);
    }

    public final Command getCalibrateOffsetRetractCommand() {
        return new NamedRunCommand( getName() + "-calibrate", this::calibrateOffsetRetracted);
    }
}