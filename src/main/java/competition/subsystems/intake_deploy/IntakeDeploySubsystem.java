package competition.subsystems.intake_deploy;

import competition.electrical_contract.ElectricalContract;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.Current;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.controls.actuators.XCANMotorControllerPIDProperties;
import xbot.common.controls.sensors.XAbsoluteEncoder;
import xbot.common.logic.Latch;
import xbot.common.properties.AngleProperty;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import xbot.common.controls.sensors.XDigitalInput;
import xbot.common.resiliency.DeviceHealth;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.Rotations;
import static edu.wpi.first.units.Units.RotationsPerSecond;
import static edu.wpi.first.units.Units.Second;


@Singleton
public class IntakeDeploySubsystem extends BaseSetpointSubsystem<Angle,Double>  {
    ElectricalContract electricalContract;
    public final XCANMotorController intakeDeployMotor;
    public final XAbsoluteEncoder intakeDeployEncoder;
    public final DoubleProperty manualControlPower;
    public Angle motorOffset = Rotations.zero();
    public final XDigitalInput intakeDeploySensor;
    public final XDigitalInput intakeDeployExtendedSensor;

    public boolean isCalibrated = false;
    public final DoubleProperty extendedPosition;
    public final DoubleProperty retractedPosition;
    private final AngleProperty mechanismTargetRotation;
    public final DoubleProperty mechanismDegreePerMotorRotation;
    public final DoubleProperty maxPidVelocity;
    public final DoubleProperty maxPidAcceleration;
    public final DoubleProperty collectionDownwardPressure;

    private final Latch extendedPositionCalibrationLatch;

    @Inject
    public IntakeDeploySubsystem(XCANMotorController.XCANMotorControllerFactory xcanMotorControllerFactory,
                                 XAbsoluteEncoder.XAbsoluteEncoderFactory xAbsoluteEncoderFactory,
                                 ElectricalContract electricalContract, PropertyFactory propertyFactory,
                                 XDigitalInput.XDigitalInputFactory xDigitalInputFactory) {
        propertyFactory.setPrefix(this);

        var defaultPIDProperties = new XCANMotorControllerPIDProperties.Builder()
                .withP(5.0)
                .withI(0.0)
                .withD(0.0)
                .withMinPowerOutput(-0.2)
                .withMaxPowerOutput(0.2)
                .build();

        if (electricalContract.isIntakeDeployReady()) {
            this.intakeDeployMotor = xcanMotorControllerFactory.create(electricalContract.getIntakeDeployMotor(),
                    getPrefix(), "IntakeDeployPID", defaultPIDProperties);

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

        if (electricalContract.intakeDeploySensorReady()){
            this.intakeDeploySensor = xDigitalInputFactory.create(
                    electricalContract.getIntakeDeploySensor(),
                    getPrefix()
            );
            this.registerDataFrameRefreshable(this.intakeDeploySensor);
        } else {
            this.intakeDeploySensor = null;
        }
        if (electricalContract.isIntakeDeployExtendedSensorReady()) {
            this.intakeDeployExtendedSensor = xDigitalInputFactory.create(
                    electricalContract.getIntakeDeployExtendedSensor(),
                    getPrefix()
            );
            this.registerDataFrameRefreshable(this.intakeDeployExtendedSensor);
        } else {
            this.intakeDeployExtendedSensor = null;
        }

        this.retractedPosition = propertyFactory.createPersistentProperty("RetractedPosition", 0.0);
        this.extendedPosition = propertyFactory.createPersistentProperty("ExtendedPosition", -185.0);

        this.manualControlPower = propertyFactory.createPersistentProperty("ManualControlPower", 0.2);

        this.mechanismDegreePerMotorRotation = propertyFactory.createPersistentProperty("MechanismDegreePerMotorRotation", 360);
        this.mechanismTargetRotation = propertyFactory.createPersistentProperty("MechanismTargetRotation", Degrees.of(0));

        this.maxPidVelocity = propertyFactory.createPersistentProperty("PidMaxMotorVelocity-RotationsPerSecond", 100);
        this.maxPidAcceleration = propertyFactory.createPersistentProperty("PidMaxMotorAcceleration-RotationsPerSecondPerSecond", 300);

        this.collectionDownwardPressure = propertyFactory.createPersistentProperty("Collection Downward Pressure Power", -0.1);

        if (this.intakeDeployMotor != null) {
            this.intakeDeployMotor.setTrapezoidalProfileMaxVelocity(RotationsPerSecond.of(maxPidVelocity.get()));
            this.intakeDeployMotor.setTrapezoidalProfileAcceleration(RotationsPerSecond.per(Second).of(maxPidAcceleration.get()));
        }

        this.extendedPositionCalibrationLatch = new Latch(false, Latch.EdgeType.RisingEdge);
        this.extendedPositionCalibrationLatch.setObserver((edgeType) -> calibrateOffsetDown());
    }

    @Override
    public Angle getCurrentValue() {
        if (intakeDeployEncoder != null) {
            return intakeDeployEncoder.getAbsolutePosition();
        }

        return Degrees.of(
                intakeDeployMotor.getPosition().minus(motorOffset).in(Rotations) * mechanismDegreePerMotorRotation.get()
        );
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
        if (this.intakeDeployEncoder != null && this.intakeDeployEncoder.getHealth() == DeviceHealth.Healthy) {
            return true;
        }

        return false;
    }

    @Override
    protected boolean areTwoTargetsEquivalent(Angle target1, Angle target2) {
        return Math.abs(target1.in(Degrees) - target2.in(Degrees)) < 0.001;
    }

    public boolean isTouchingIntakeDeploy() {
        if (intakeDeploySensor != null) {
            return this.intakeDeploySensor.get();
        }
        return false;
    }

    public boolean isTouchingIntakeDeployExtendedSensor() {
        if (intakeDeployExtendedSensor != null) {
            return this.intakeDeployExtendedSensor.get();
        }
        return false;
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

        // When this becomes true, calibrateOffsetDown() will be called
        this.extendedPositionCalibrationLatch.setValue(isTouchingIntakeDeployExtendedSensor());

        if (isTouchingIntakeDeploy() && !isCalibrated) {
            calibrateOffsetUp();
        }

        if (isTouchingIntakeDeployExtendedSensor() && !isCalibrated) {
            calibrateOffsetDown();
        }

        aKitLog.record("IsCalibrated", isCalibrated);
        aKitLog.record("CurrentPosition", getCurrentValue().in(Degrees));
    }

    public void calibrateOffsetDown() {
        if (intakeDeployMotor != null) {
            // calculate the motorOffset such that the current position is extendedPosition.get() degrees
            motorOffset = intakeDeployMotor.getPosition().minus(
                    Rotations.of(extendedPosition.get() / mechanismDegreePerMotorRotation.get())
            );
            isCalibrated = true;
        }
    }

    public void calibrateOffsetUp() {
        if (intakeDeployMotor != null) {
            motorOffset = intakeDeployMotor.getPosition();
            setTargetValue(getCurrentValue());
            isCalibrated = true;
        }
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
}
