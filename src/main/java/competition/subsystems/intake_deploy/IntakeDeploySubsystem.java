package competition.subsystems.intake_deploy;

import competition.electrical_contract.ElectricalContract;
import edu.wpi.first.units.measure.Angle;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.controls.actuators.XCANMotorControllerPIDProperties;
import xbot.common.properties.AngleProperty;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import xbot.common.controls.sensors.XDigitalInput;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.RPM;
import static edu.wpi.first.units.Units.Rotations;
import static edu.wpi.first.units.Units.RotationsPerSecond;
import static edu.wpi.first.units.Units.Second;


@Singleton
public class IntakeDeploySubsystem extends BaseSetpointSubsystem<Angle,Double>  {
   ElectricalContract electricalContract;
    public final XCANMotorController intakeDeployMotor;
    public final DoubleProperty manualControlPower;
    public Angle motorOffset = Rotations.zero();
    public final XDigitalInput intakeDeploySensor;

    public boolean isCalibrated = false;
    public final DoubleProperty extendedPosition;
    public final DoubleProperty retractedPosition;
    private final AngleProperty mechanismTargetRotation;
    public final DoubleProperty mechanismDegreePerMotorRotation;
    public final DoubleProperty maxPidVelocity;
    public final DoubleProperty maxPidAcceleration;

    // Limb range is the rotations between the Deploy's position and the stowed position, used for calibration.
    public final AngleProperty limbRange;

    @Inject
    public IntakeDeploySubsystem(XCANMotorController.XCANMotorControllerFactory xcanMotorControllerFactory,
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

        if (electricalContract.intakeDeploySensorReady()){
            this.intakeDeploySensor = xDigitalInputFactory.create(
                    electricalContract.getIntakeDeploySensor(),
                    getPrefix()
            );
            this.registerDataFrameRefreshable(this.intakeDeploySensor);
        } else {
            this.intakeDeploySensor = null;
        }

        this.retractedPosition = propertyFactory.createPersistentProperty("RetractedPosition", 0.0);
        this.extendedPosition = propertyFactory.createPersistentProperty("ExtendedPosition", -145.0);

        this.manualControlPower = propertyFactory.createPersistentProperty("ManualControlPower", 0.2);
        this.limbRange = propertyFactory.createPersistentProperty("limbRange", Rotations.of(9.5));

        this.mechanismDegreePerMotorRotation = propertyFactory.createPersistentProperty("MechanismDegreePerMotorRotation", 15);
        this.mechanismTargetRotation = propertyFactory.createPersistentProperty("MechanismTargetRotation", Degrees.of(0));

        this.maxPidVelocity = propertyFactory.createPersistentProperty("PidMaxMotorVelocity-RotationsPerSecond", 100);
        this.maxPidAcceleration = propertyFactory.createPersistentProperty("PidMaxMotorAcceleration-RotationsPerSecondPerSecond", 300);

        if (this.intakeDeployMotor != null) {
            this.intakeDeployMotor.setTrapezoidalProfileMaxVelocity(RotationsPerSecond.of(maxPidVelocity.get()));
            this.intakeDeployMotor.setTrapezoidalProfileAcceleration(RotationsPerSecond.per(Second).of(maxPidAcceleration.get()));
        }
    }

    @Override
    public Angle getCurrentValue() {
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

            intakeDeployMotor.setPositionTarget(
                    Rotations.of(goal.in(Degrees) / mechanismDegreePerMotorRotation.get()).plus(motorOffset),
                    XCANMotorController.MotorPidMode.TrapezoidalVoltage
            );
        }
    }

    @Override
    public boolean isCalibrated() {
        return this.isCalibrated;
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
        // Sensor reading seems bad - don't trust it for now
        //if (isTouchingIntakeDeploy() && !isCalibrated) {
        //    calibrateOffsetUp();
        //}

        aKitLog.record("IsCalibrated", isCalibrated);
        aKitLog.record("CurrentPosition", getCurrentValue().in(Degrees));
        aKitLog.record("TargetPosition", getTargetValue().in(Degrees));
    }

    public void calibrateOffsetDown() {
        if (intakeDeployMotor != null) {
            motorOffset = intakeDeployMotor.getPosition().minus(limbRange.get());
            setTargetValue(getCurrentValue());
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
}
