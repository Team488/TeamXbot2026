package competition.subsystems.climber;

import competition.electrical_contract.ElectricalContract;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.wpilibj2.command.Command;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.command.NamedInstantCommand;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.controls.actuators.XCANMotorControllerPIDProperties;
import xbot.common.properties.AngleProperty;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.Rotations;
import static edu.wpi.first.units.Units.RotationsPerSecond;
import static edu.wpi.first.units.Units.Second;

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

    public final AngleProperty retractedAngle;
    public final AngleProperty extendedAngle;
    public final AngleProperty engagedAngle;

    public final DoubleProperty maxPidVelocity;
    public final DoubleProperty maxPidAcceleration;

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

        var defaultPIDProperties = new XCANMotorControllerPIDProperties.Builder()
                .withP(0.6)
                .withI(0.0)
                .withD(0.0)
                .withMinPowerOutput(-1.0)
                .withMaxPowerOutput(1.0)
                .build();

        if (electricalContract.isClimberLeftReady() && electricalContract.isClimberRightReady()) {
            this.climberMotorLeft = motorFactory.create(electricalContract.getClimberMotorLeft(),
                    getPrefix(), "ClimberMotorPID", defaultPIDProperties);

            this.registerDataFrameRefreshable(climberMotorLeft);

            this.climberMotorRight = motorFactory.create(
                    electricalContract.getClimberMotorRight(),
                    getPrefix(), "ClimberMotorPID", defaultPIDProperties);

            this.registerDataFrameRefreshable(climberMotorRight);
        } else {
            this.climberMotorLeft = null;
            this.climberMotorRight = null;
        }


        this.mechanismDegreesPerMotorRotation = propertyFactory.createPersistentProperty("MechanismDegreesPerMotorRotation", 3.0);
        this.manualControlPower = propertyFactory.createPersistentProperty("ManualControlPower", 0.1);

        this.extendPower = propertyFactory.createPersistentProperty("ExtendPower", 0.2);
        this.retractPower = propertyFactory.createPersistentProperty("RetractPower", -0.2);

        this.retractedAngle = propertyFactory.createPersistentProperty("RetractedAngle", Degrees.of(0));
        this.extendedAngle = propertyFactory.createPersistentProperty("ExtendedAngle", Degrees.of(180));
        this.engagedAngle = propertyFactory.createPersistentProperty("ClimbEngagedAngle", Degrees.of(85));

        this.maxPidVelocity = propertyFactory.createPersistentProperty("PidMaxMotorVelocity-RotationsPerSecond", 100);
        this.maxPidAcceleration = propertyFactory.createPersistentProperty("PidMaxMotorAcceleration-RotationsPerSecondPerSecond", 300);

        this.mechanismTargetAngle = propertyFactory.createPersistentProperty("MechanismTargetAngle", Degrees.zero());

        if (this.climberMotorLeft != null) {
            this.climberMotorLeft.setTrapezoidalProfileMaxVelocity(RotationsPerSecond.of(maxPidVelocity.get()));
            this.climberMotorLeft.setTrapezoidalProfileAcceleration(RotationsPerSecond.per(Second).of(maxPidAcceleration.get()));
        }
        if (this.climberMotorRight != null) {
            this.climberMotorRight.setTrapezoidalProfileMaxVelocity(RotationsPerSecond.of(maxPidVelocity.get()));
            this.climberMotorRight.setTrapezoidalProfileAcceleration(RotationsPerSecond.per(Second).of(maxPidAcceleration.get()));
        }
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

        if (this.maxPidVelocity.hasChangedSinceLastCheck()) {
            if (climberMotorLeft != null) {
                this.climberMotorLeft.setTrapezoidalProfileMaxVelocity(RotationsPerSecond.of(maxPidVelocity.get()));
            }
            if (climberMotorRight != null) {
                this.climberMotorRight.setTrapezoidalProfileMaxVelocity(RotationsPerSecond.of(maxPidVelocity.get()));
            }
        }

        if (this.maxPidAcceleration.hasChangedSinceLastCheck()) {
            if (climberMotorLeft != null) {
                this.climberMotorLeft.setTrapezoidalProfileAcceleration(RotationsPerSecond.per(Second).of(maxPidAcceleration.get()));
            }
            if (climberMotorRight != null) {
                this.climberMotorRight.setTrapezoidalProfileAcceleration(RotationsPerSecond.per(Second).of(maxPidAcceleration.get()));
            }
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
            setTargetValue(getCurrentValue());
            isCalibrated = true;
        }
    }

    public void setPositionalGoalIncludingOffset(Angle setpoint) {
        if (climberMotorRight != null) {
            climberMotorRight.setPositionTarget(
                    Rotations.of(setpoint.in(Degrees) / mechanismDegreesPerMotorRotation.get()).plus(motorOffset),
                    XCANMotorController.MotorPidMode.TrapezoidalVoltage);
        }

        if (climberMotorLeft != null) {
            climberMotorLeft.setPositionTarget(
                    Rotations.of(setpoint.in(Degrees) / mechanismDegreesPerMotorRotation.get()).plus(motorOffset),
                    XCANMotorController.MotorPidMode.TrapezoidalVoltage);
        }
    }

    public final Command getCalibrateOffsetRetractCommand() {
        return new NamedInstantCommand( getName() + "-calibrate", this::calibrateOffsetRetracted) {
            @Override
            public boolean runsWhenDisabled() {
                return true;
            }
        };
    }
}