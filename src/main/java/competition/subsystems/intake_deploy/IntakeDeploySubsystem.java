package competition.subsystems.intake_deploy;

import competition.electrical_contract.ElectricalContract;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.wpilibj2.command.Command;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.controls.actuators.XCANMotorControllerPIDProperties;
import xbot.common.controls.sensors.XAbsoluteEncoder;
import xbot.common.properties.AngleProperty;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.command.SimpleWaitForMaintainerCommand;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.RotationsPerSecond;
import static edu.wpi.first.units.Units.Second;
import static edu.wpi.first.units.Units.Seconds;


@Singleton
public class IntakeDeploySubsystem extends BaseSetpointSubsystem<Angle,Double>  {
    public final XCANMotorController intakeDeployMotor;
    public final XAbsoluteEncoder intakeDeployEncoder;
    public final DoubleProperty manualControlPower;

    public boolean isCalibrated = false;
    public final DoubleProperty extendedPosition;
    public final DoubleProperty retractedPosition;
    private final AngleProperty mechanismTargetRotation;
    public final DoubleProperty mechanismDegreePerMotorRotation;
    public final DoubleProperty maxPidVelocity;
    public final DoubleProperty maxPidAcceleration;
    public final DoubleProperty collectionDownwardPressure;
    public final DoubleProperty voltageRampTime;
    public DoubleProperty readinessTimeoutSeconds;

    @Inject
    public IntakeDeploySubsystem(XCANMotorController.XCANMotorControllerFactory xcanMotorControllerFactory,
                                 XAbsoluteEncoder.XAbsoluteEncoderFactory xAbsoluteEncoderFactory,
                                 ElectricalContract electricalContract, PropertyFactory propertyFactory) {
        propertyFactory.setPrefix(this);

        var defaultPIDProperties = new XCANMotorControllerPIDProperties.Builder()
                .withP(40.0)
                .withI(0.0)
                .withD(0.0)
                .withMinPowerOutput(-0.4)
                .withMaxPowerOutput(0.4)
                .build();

        this.voltageRampTime = propertyFactory.createPersistentProperty("VoltageRampTime", 0.1);

        if (electricalContract.isIntakeDeployReady()) {
            this.intakeDeployMotor = xcanMotorControllerFactory.create(electricalContract.getIntakeDeployMotor(),
                    getPrefix(), "IntakeDeployPID", defaultPIDProperties);
            this.intakeDeployMotor.setOpenLoopRampRates(
                    Seconds.of(voltageRampTime.get()),
                    Seconds.of(voltageRampTime.get()));
            this.intakeDeployMotor.setClosedLoopRampRates(
                    Seconds.of(voltageRampTime.get()),
                    Seconds.of(voltageRampTime.get()));
            this.registerDataFrameRefreshable(this.intakeDeployMotor);
        } else {
            this.intakeDeployMotor = null;
        }

        if (electricalContract.isIntakeDeployAbsoluteEncoderReady()) {
            this.intakeDeployEncoder = xAbsoluteEncoderFactory.create(
                    electricalContract.getIntakeDeployAbsoluteEncoder(),
                    this.getPrefix()
            );
            registerDataFrameRefreshable(this.intakeDeployEncoder);
        } else {
            this.intakeDeployEncoder = null;
        }

        this.retractedPosition = propertyFactory.createPersistentProperty("RetractedPosition", -10.0);
        this.extendedPosition = propertyFactory.createPersistentProperty("ExtendedPosition", -145.0);

        this.manualControlPower = propertyFactory.createPersistentProperty("ManualControlPower", 0.2);

        this.mechanismDegreePerMotorRotation = propertyFactory.createPersistentProperty("MechanismDegreePerMotorRotation", 360);
        this.mechanismTargetRotation = propertyFactory.createPersistentProperty("MechanismTargetRotation", Degrees.of(-10));

        this.maxPidVelocity = propertyFactory.createPersistentProperty("PidMaxMotorVelocity-RotationsPerSecond", 200);
        this.maxPidAcceleration = propertyFactory.createPersistentProperty("PidMaxMotorAcceleration-RotationsPerSecondPerSecond", 200);

        this.collectionDownwardPressure = propertyFactory.createPersistentProperty("Collection Downward Pressure Power", -0.1);
        this.readinessTimeoutSeconds = propertyFactory.createPersistentProperty("Readiness Timeout Seconds", 3.0);

        if (this.intakeDeployMotor != null) {
            this.intakeDeployMotor.setTrapezoidalProfileMaxVelocity(RotationsPerSecond.of(maxPidVelocity.get()));
            this.intakeDeployMotor.setTrapezoidalProfileAcceleration(RotationsPerSecond.per(Second).of(maxPidAcceleration.get()));
        }
    }

    @Override
    public Angle getCurrentValue() {
        if (intakeDeployEncoder != null) {
            return intakeDeployEncoder.getAbsolutePosition();
        }


        return Degrees.zero();
    }

    @Override
    public Angle getTargetValue() {
        return mechanismTargetRotation.get();
    }

    @Override
    public void setTargetValue(Angle value) {
        this.mechanismTargetRotation.set(value);
    }

    @Override
    public void setPower(Double power) {
        if (intakeDeployMotor != null) {
            intakeDeployMotor.setPower(power * this.manualControlPower.get());
        }
    }

    public void setPositionGoal(Angle goal) {
        if (intakeDeployMotor != null) {
            if (!isCalibrated()) {
                log.warn("Attempted to set position goal while not calibrated!");
                return;
            }

            // With CANCoder as a remote sensor, we can directly set the position target in degrees without needing
            // to convert to motor rotations
            intakeDeployMotor.setPositionTarget(goal, XCANMotorController.MotorPidMode.TrapezoidalVoltage);
        }
    }

    @Override
    public boolean isCalibrated() {
        return this.intakeDeployEncoder != null;
    }

    @Override
    protected boolean areTwoTargetsEquivalent(Angle target1, Angle target2) {
        return Math.abs(target1.in(Degrees) - target2.in(Degrees)) < 0.001;
    }

    public void stop() {
        if (intakeDeployMotor != null) {
            intakeDeployMotor.setPower(0);
        }
    }

    public void periodic() {
        if (intakeDeployMotor != null) {
            intakeDeployMotor.periodic();

            if (this.maxPidVelocity.hasChangedSinceLastCheck()) {
                this.intakeDeployMotor.setTrapezoidalProfileMaxVelocity(RotationsPerSecond.of(maxPidVelocity.get()));
            }

            if (this.maxPidAcceleration.hasChangedSinceLastCheck()) {
                this.intakeDeployMotor.setTrapezoidalProfileAcceleration(RotationsPerSecond.per(Second).of(maxPidAcceleration.get()));
            }
        }

        aKitLog.record("CurrentPosition", getCurrentValue().in(Degrees));
        aKitLog.record("TargetPosition", getTargetValue().in(Degrees));
    }

    public void intakeDeployGoDown() {
        if (intakeDeployMotor != null) {
            setTargetValue(getCurrentValue().minus(Degrees.of(3.0)));
        }
    }

    public void intakeDeployGoUp() {
        if (intakeDeployMotor != null) {
            setTargetValue(getCurrentValue().plus(Degrees.of(3.0)));
        }
    }

    public Current getMotorCurrent() {
        if (intakeDeployMotor != null) {
            return intakeDeployMotor.getCurrent();
        }
        return Amps.zero();
    }

    public Command getWaitForAtGoalCommand() {
        return new SimpleWaitForMaintainerCommand(this, () -> readinessTimeoutSeconds.get());
    }
}
