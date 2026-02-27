package competition.subsystems.climber;

import competition.electrical_contract.ElectricalContract;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.MutAngle;
import edu.wpi.first.wpilibj2.command.Command;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.command.NamedRunCommand;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.controls.actuators.XCANMotorControllerPIDProperties;
import xbot.common.controls.sensors.XDigitalInput;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.Rotations;

@Singleton
public class ClimberSubsystem extends BaseSetpointSubsystem <Angle, Double> {

    ElectricalContract electricalContract;
    public final XCANMotorController climberMotorLeft;
    public final XCANMotorController climberMotorRight;
    public final XDigitalInput climberSensor;
    private final DoubleProperty mechanismDegreesPerMotorRotation;
    public final DoubleProperty manualControlPower;
    public DoubleProperty extendPower;
    public DoubleProperty retractPower;
    public ClimberState climberState;
    public Angle motorOffset = Degrees.zero();
    private boolean isCalibrated;

    private final MutAngle mechanismTargetAngle = Degrees.mutable(0);

    public enum ClimberState {
        EXTENDING,
        RETRACTING,
        STOPPED
    }

    @Inject
    public ClimberSubsystem(XCANMotorController.XCANMotorControllerFactory motorFactory,
                            ElectricalContract electricalContract, PropertyFactory propertyFactory,
                            XDigitalInput.XDigitalInputFactory xDigitalInputFactory) {
        propertyFactory.setPrefix(this);
        this.electricalContract = electricalContract;

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

        if (electricalContract.isClimberSensorReady()) {
            this.climberSensor = xDigitalInputFactory.create(
                    electricalContract.getClimberSensor(),
                    this.getPrefix());
            this.registerDataFrameRefreshable(climberSensor);
        } else {
            this.climberSensor = null;
        }
        this.mechanismDegreesPerMotorRotation = propertyFactory.createPersistentProperty("MechanismDegreesPerMotorRotation", 0);
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

    public boolean isTouchingSensor() {
        if (electricalContract.isClimberSensorReady()) {
            return this.climberSensor.get();
        }
        return false;
    }

    public void periodic() {

        if (isTouchingSensor() && !isCalibrated) {
            calibrateOffsetRetracted();
        }

        if (climberMotorLeft != null) {
            climberMotorLeft.periodic();
        }

        if (climberMotorRight != null) {
            climberMotorRight.periodic();
        }
    }

    @Override
    public Angle getCurrentValue() {
        return Degrees.of(
                climberMotorLeft.getPosition().minus(motorOffset).in(Rotations) * mechanismDegreesPerMotorRotation.get()
        );
    }

    @Override
    public Angle getTargetValue() {
        return mechanismTargetAngle.copy();
    }

    @Override
    public void setTargetValue(Angle angle) {
       mechanismTargetAngle.mut_replace(angle);
    }

    @Override
    public void setPower(Double power) {}

    private Angle getCalibratedPosition() {
        return getCurrentValue().minus(motorOffset);
    }

    @Override
    public boolean isCalibrated() {
        return isCalibrated;
    }

    @Override
    protected boolean areTwoTargetsEquivalent(Angle target1, Angle target2) {
        return Math.abs(target1.in(Rotations)-target2.in(Rotations)) < .01;
    }

    public void calibrateOffsetRetracted() {
        if (climberMotorLeft != null) {
            motorOffset = climberMotorLeft.getPosition();
            isCalibrated = true;
        }
    }

    public void setPositionalGoalIncludingOffset(Angle setpoint) {
        if (climberMotorRight != null) {
            climberMotorRight.setPositionTarget(
                    Rotations.of(setpoint.in(Degrees) / mechanismDegreesPerMotorRotation.get()).plus(motorOffset),
                    XCANMotorController.MotorPidMode.Voltage);
        }

        if (climberMotorLeft != null) {
            climberMotorLeft.setPositionTarget(
                    Rotations.of(setpoint.in(Degrees) / mechanismDegreesPerMotorRotation.get()).plus(motorOffset),
                    XCANMotorController.MotorPidMode.Voltage);
        }
    }

    public final Command getCalibrateOffsetRetractCommand() {
        return new NamedRunCommand( getName() + "-calibrate", this::calibrateOffsetRetracted);
    }
}