package competition.subsystems.intake_deploy;

import competition.electrical_contract.ElectricalContract;
import edu.wpi.first.units.measure.Angle;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.controls.actuators.XCANMotorControllerPIDProperties;
import xbot.common.properties.AngleProperty;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.Rotation;
import static edu.wpi.first.units.Units.Rotations;


@Singleton
public class IntakeDeploySubsystem extends BaseSetpointSubsystem<Angle,Double>  {
    public final XCANMotorController intakeDeployMotor;
    public final DoubleProperty manualControlPower;
    public final AngleProperty limbRange; //limb range is the rotations between the deploy position and the stowed position, used for calibration
    public Angle motorOffset;
    public boolean isCalibrated = false;
    public final DoubleProperty extendedPositionInDegree;
    public final DoubleProperty retractedPositionInDegree;
    private final AngleProperty targetRotation;



    public final DoubleProperty degreesPerRotation;

    @Inject
    public IntakeDeploySubsystem(XCANMotorController.XCANMotorControllerFactory xcanMotorControllerFactory,
                                 ElectricalContract electricalContract, PropertyFactory propertyFactory) {
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

        this.retractedPositionInDegree = propertyFactory.createPersistentProperty("RetractedPositionDegrees", 0);
        this.extendedPositionInDegree = propertyFactory.createPersistentProperty("ExtendedPositionDegrees", 90);
        this.manualControlPower = propertyFactory.createPersistentProperty("ManualControlPower", 0.1);
        this.limbRange = propertyFactory.createPersistentProperty("limbRange", Rotations.of(9.5));

        this.degreesPerRotation = propertyFactory.createPersistentProperty("DegreesPerRotation", 360);
        this.targetRotation = propertyFactory.createPersistentProperty("TargetRotation", Degrees.of(0));
    }

    @Override
    public Angle getCurrentValue() {
        return Degrees.of(intakeDeployMotor.getPosition().minus(motorOffset).in(Rotations) * degreesPerRotation.get());
    }

    @Override
    public Angle getTargetValue() {
        return targetRotation.get();
    }

    @Override
    public void setTargetValue(Angle value) {
        this.targetRotation.set(value);
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
                    Rotations.of(goal.in(Degrees) / degreesPerRotation.get()).plus(motorOffset)
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
