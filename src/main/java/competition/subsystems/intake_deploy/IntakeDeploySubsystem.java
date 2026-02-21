package competition.subsystems.intake_deploy;

import competition.electrical_contract.ElectricalContract;
import edu.wpi.first.units.measure.Angle;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.properties.AngleProperty;
import xbot.common.command.BaseSetpointSubsystem;

import xbot.common.math.PIDManager;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;
import xbot.common.controls.sensors.XAbsoluteEncoder;
import xbot.common.resiliency.DeviceHealth;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.Rotations;


@Singleton
public class IntakeDeploySubsystem extends BaseSetpointSubsystem<Angle,Double>  {
    public final XCANMotorController intakeDeployMotor;
    public final XAbsoluteEncoder intakeDeployAbsoluteEncoder;
    public final DoubleProperty manualControlPower;
    public final AngleProperty limbRange;
    public Angle offset;
    public boolean isCalibrated = false;
    public final DoubleProperty extendedPositionInDegree;
    public final DoubleProperty retractedPositionInDegree;
    private Angle targetRotation;

    public final DoubleProperty degreesPerRotation;

    @Inject
    public IntakeDeploySubsystem(XCANMotorController.XCANMotorControllerFactory xcanMotorControllerFactory,
                                 ElectricalContract electricalContract, PropertyFactory propertyFactory, PIDManager.PIDManagerFactory pidManagerFactory,
                                 XAbsoluteEncoder.XAbsoluteEncoderFactory xAbsoluteEncoderFactory) {
        propertyFactory.setPrefix(this);

        if (electricalContract.isIntakeDeployReady()) {
            this.intakeDeployMotor = xcanMotorControllerFactory.create(electricalContract.getIntakeDeployMotor(),
                    getPrefix(), "intakeDeploy");
            this.registerDataFrameRefreshable(this.intakeDeployMotor);
        } else {
            this.intakeDeployMotor = null;
        }

        if (electricalContract.isIntakeDeployAbsoluteEncoderReady()) {
            this.intakeDeployAbsoluteEncoder = xAbsoluteEncoderFactory.create
                    (electricalContract.getIntakeDeployAbsoluteEncoderMotor(),
                            getPrefix());
            registerDataFrameRefreshable(intakeDeployAbsoluteEncoder);
        } else {
            this.intakeDeployAbsoluteEncoder = null;
        }

        this.retractedPositionInDegree = propertyFactory.createPersistentProperty("RetractedPositionDegrees", 0);
        this.extendedPositionInDegree = propertyFactory.createPersistentProperty("ExtendedPositionDegrees", 90);
        this.manualControlPower = propertyFactory.createPersistentProperty("ManualControlPower", 0.1);
        this.limbRange = propertyFactory.createPersistentProperty("limbRange", Degrees.of(85));

        this.degreesPerRotation = propertyFactory.createPersistentProperty("DegreesPerRotation", 360);
        this.targetRotation = getCurrentValue();
    }

    @Override
    public Angle getCurrentValue() {
        if (intakeDeployAbsoluteEncoder != null) {
            return intakeDeployAbsoluteEncoder.getAbsolutePosition();
        }
        return Degrees.zero();
    }

    @Override
    public Angle getTargetValue() {
        return targetRotation;
    }

    @Override
    public void setTargetValue(Angle value) {
        this.targetRotation = value;
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
                    Rotations.of(goal.in(Degrees) / degreesPerRotation.get())
            );
        }
    }

    @Override
    public boolean isCalibrated() {
        return intakeDeployAbsoluteEncoder != null
                && intakeDeployAbsoluteEncoder.getHealth() == DeviceHealth.Healthy;
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
        }
    }

    public void calibrateOffsetDown() {
        if (intakeDeployMotor != null) {
            offset = intakeDeployMotor.getPosition().minus(limbRange.get());
            isCalibrated = true;
        }
    }

    public void calibrateOffsetUp() {
        if (intakeDeployMotor != null) {
            offset = intakeDeployMotor.getPosition();
            isCalibrated = true;
        }
    }
}
