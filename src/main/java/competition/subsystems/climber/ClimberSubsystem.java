package competition.subsystems.climber;

import competition.electrical_contract.ElectricalContract;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.MutAngle;
import edu.wpi.first.wpilibj2.command.Command;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.command.NamedRunCommand;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.controls.actuators.XCANMotorControllerPIDProperties;
import xbot.common.properties.AngleProperty;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.Rotations;

@Singleton
public class ClimberSubsystem extends BaseSetpointSubsystem<Angle, Double> {
    public final XCANMotorController climberMotorLeft;
    public final XCANMotorController climberMotorRight;
    public final DoubleProperty mechanismDegreesPerMotorRotation;
    public final DoubleProperty manualControlPower;
    public DoubleProperty extendPower;
    public DoubleProperty retractPower;
    public ClimberState climberState;
    public Angle motorOffset = Degrees.zero();
    private boolean isCalibrated;

    private final AngleProperty mechanismTargetAngle;

    public enum ClimberState {
        EXTENDING,
        RETRACTING,
        STOPPED
    }

    @Inject
    public ClimberSubsystem(XCANMotorController.XCANMotorControllerFactory motorFactory,
                            ElectricalContract electricalContract, PropertyFactory propertyFactory) {
        propertyFactory.setPrefix(this);

        var defaultPIDProperties1 = new XCANMotorControllerPIDProperties.Builder()
                .withP(5.0)
                .withI(0.0)
                .withD(0.0)
                .withMinPowerOutput(-0.2)
                .withMaxPowerOutput(0.2)
                .build();

        var defaultPIDProperties2 = new XCANMotorControllerPIDProperties.Builder()
                .withP(5.0)
                .withI(0.0)
                .withD(0.0)
                .withMinPowerOutput(-0.2)
                .withMaxPowerOutput(0.2)
                .build();

        if (electricalContract.isClimberLeftReady() && electricalContract.isClimberRightReady()) {
            this.climberMotorLeft = motorFactory.create(electricalContract.getClimberMotorLeft(),
                    getPrefix(), "ClimberMotorPID", defaultPIDProperties1);

            this.registerDataFrameRefreshable(climberMotorLeft);

            this.climberMotorRight = motorFactory.create(
                    electricalContract.getClimberMotorRight(),
                    getPrefix(), "ClimberMotorPID", defaultPIDProperties2);

            this.registerDataFrameRefreshable(climberMotorRight);
        } else {
            this.climberMotorLeft = null;
            this.climberMotorRight = null;
        }


        this.mechanismDegreesPerMotorRotation = propertyFactory.createPersistentProperty("MechanismDegreesPerMotorRotation", 3.0);
        this.manualControlPower = propertyFactory.createPersistentProperty("ManualControlPower", 0.1);

        this.extendPower = propertyFactory.createPersistentProperty("ExtendPower", 0.2);
        this.retractPower = propertyFactory.createPersistentProperty("RetractPower", -0.2);

        // TODO: Figure out mech deg per motor rot
        this.mechanismTargetAngle = propertyFactory.createPersistentProperty("MechanismTargetAngle", Degrees.zero());

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

        aKitLog.record("TargetPosition", getTargetValue());
        aKitLog.record("CurrentPosition", getCurrentValue());

        this.isCalibrated = true;

        if (climberMotorLeft != null) {
            climberMotorLeft.periodic();
        }
        if (climberMotorRight != null) {
            climberMotorRight.periodic();
        }

        aKitLog.record("IsCalibrated", isCalibrated);
        aKitLog.record("CurrentPosition", getCurrentValue());
    }

    @Override
    public Angle getCurrentValue() {
        return Degrees.of(
                climberMotorLeft.getPosition().minus(motorOffset).in(Rotations) * mechanismDegreesPerMotorRotation.get()
        );
    }

    @Override
    public Angle getTargetValue() {
        return mechanismTargetAngle.get();
    }

    @Override
    public void setTargetValue(Angle angle) {
       mechanismTargetAngle.set(angle);
    }

    @Override
    public void setPower(Double power) {}

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