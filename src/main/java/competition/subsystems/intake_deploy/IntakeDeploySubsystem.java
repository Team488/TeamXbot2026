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
import static edu.wpi.first.units.Units.Rotations;


@Singleton
public class IntakeDeploySubsystem extends BaseSetpointSubsystem<Angle,Double>  {
   ElectricalContract electricalContract;
    public final XCANMotorController intakeDeployMotor;
    public final DoubleProperty manualControlPower;
    public Angle motorOffset = Rotations.zero();
    public final XDigitalInput intakeDeploySensor;

    public boolean isCalibrated = false;
    public final AngleProperty extendedPosition;
    public final AngleProperty retractedPosition;
    private final AngleProperty mechanismTargetRotation;
    public final DoubleProperty mechanismDegreePerMotorRotation;

    // Limb range is the rotations between the Deploy's position and the stowed position, used for calibration.
    public final AngleProperty limbRange;

    @Inject
    public IntakeDeploySubsystem(XCANMotorController.XCANMotorControllerFactory xcanMotorControllerFactory,
                                 ElectricalContract electricalContract, PropertyFactory propertyFactory,
                                 XDigitalInput.XDigitalInputFactory xDigitalInputFactory, XDigitalInput intakeDeploySensor) {
        propertyFactory.setPrefix(this);

        var defaultPIDProperties = new XCANMotorControllerPIDProperties.Builder()
                .withP(0.0)
                .withI(0.0)
                .withD(0.0)
                .withMinPowerOutput(-1.0)
                .withMaxPowerOutput(1.0)
                .build();

        if (electricalContract.isIntakeDeployReady()) {
            this.intakeDeployMotor = xcanMotorControllerFactory.create(electricalContract.getIntakeDeployMotor(),
                    getPrefix(), "IntakeDeployPID", defaultPIDProperties);
            this.registerDataFrameRefreshable(this.intakeDeployMotor);
        } else {
            this.intakeDeployMotor = null;
        }

        this.retractedPosition = propertyFactory.createPersistentProperty("RetractedPosition", Degrees.zero());
        this.extendedPosition = propertyFactory.createPersistentProperty("ExtendedPosition", Degrees.of(90));

        if (electricalContract.intakeDeploySensorReady()){
            this.intakeDeploySensor = xDigitalInputFactory.create
                    (electricalContract.getIntakeDeploySensor(),
                            getPrefix());
            registerDataFrameRefreshable(intakeDeploySensor);
        } else {
            this.intakeDeploySensor = null;
        }

        this.manualControlPower = propertyFactory.createPersistentProperty("ManualControlPower", 0.1);
        this.limbRange = propertyFactory.createPersistentProperty("limbRange", Rotations.of(9.5));

        this.mechanismDegreePerMotorRotation = propertyFactory.createPersistentProperty("MechanismDegreePerMotorRotation", 360);
        this.mechanismTargetRotation = propertyFactory.createPersistentProperty("MechanismTargetRotation", Degrees.of(0));
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
            intakeDeployMotor.setPower(power);
        }
    }

    public void setPositionGoal(Angle goal) {
        if (intakeDeployMotor != null) {
            if (!isCalibrated()) {
                log.warn("Attempted to set position goal while not calibrated!");
                return;
            }

            intakeDeployMotor.setPositionTarget(
                    Rotations.of(goal.in(Degrees) / mechanismDegreePerMotorRotation.get()).plus(motorOffset)
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
        if (electricalContract.intakeDeploySensorReady()) {
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
        aKitLog.record("IsCalibrated", isCalibrated);
        if (intakeDeployMotor != null) {
            intakeDeployMotor.periodic();
        }
        if (isTouchingIntakeDeploy()){
            calibrateOffsetUp();
        }
    }

    public void calibrateOffsetDown() {
        if (intakeDeployMotor != null) {
            motorOffset = intakeDeployMotor.getPosition().minus(limbRange.get());
            isCalibrated = true;
        }
    }

    public void calibrateOffsetUp() {
        if (intakeDeployMotor != null) {
            motorOffset = intakeDeployMotor.getPosition();
            isCalibrated = true;
        }
    }
}
