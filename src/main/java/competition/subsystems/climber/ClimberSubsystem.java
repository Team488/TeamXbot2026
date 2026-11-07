package competition.subsystems.climber;

import competition.electrical_contract.ElectricalContract;
import competition.electrical_contract.Hardware;
import edu.wpi.first.units.measure.Angle;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.controls.actuators.XCANMotorControllerPIDProperties;
import xbot.common.controls.sensors.XDigitalInput;
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

    final ElectricalContract electricalContract;

    public final XCANMotorController climberMotorLeft;
    public final XCANMotorController climberMotorRight;
    public final XDigitalInput climberSensor;

    // How many degrees the climber arm moves for each rotation of the motor
    public final DoubleProperty mechanismDegreesPerMotorRotation;
    // The angle of the climber when it is fully retracted (this is where the sensor triggers)
    public final AngleProperty retractedAngle;

    @Inject
    public ClimberSubsystem(XCANMotorController.XCANMotorControllerFactory motorFactory,
                            ElectricalContract electricalContract, PropertyFactory propertyFactory,
                            XDigitalInput.XDigitalInputFactory xDigitalInputFactory) {
        this.electricalContract = electricalContract;
        propertyFactory.setPrefix(this);

        var defaultPIDProperties = new XCANMotorControllerPIDProperties.Builder()
                .withP(0.6)
                .withI(0.0)
                .withD(0.0)
                .withMinPowerOutput(-1.0)
                .withMaxPowerOutput(1.0)
                .build();

        if (electricalContract.isReady(Hardware.ClimberLeft) && electricalContract.isReady(Hardware.ClimberRight)) {
            this.climberMotorLeft = motorFactory.create(electricalContract.getClimberMotorLeft(),
                    getPrefix(), "ClimberMotorPID", defaultPIDProperties);
            this.registerDataFrameRefreshable(climberMotorLeft);

            this.climberMotorRight = motorFactory.create(electricalContract.getClimberMotorRight(),
                    getPrefix(), "ClimberMotorPID", defaultPIDProperties);
            this.registerDataFrameRefreshable(climberMotorRight);

            // Settings for the motor's built-in "trapezoidal" motion profile, used by
            // setPositionTarget(..., MotorPidMode.TrapezoidalVoltage)
            for (var motor : new XCANMotorController[] { climberMotorLeft, climberMotorRight }) {
                motor.setTrapezoidalProfileMaxVelocity(RotationsPerSecond.of(100));
                motor.setTrapezoidalProfileAcceleration(RotationsPerSecond.per(Second).of(300));
            }
        } else {
            this.climberMotorLeft = null;
            this.climberMotorRight = null;
        }

        if (electricalContract.isReady(Hardware.ClimberSensor)) {
            this.climberSensor = xDigitalInputFactory.create(
                    electricalContract.getClimberSensor(),
                    this.getPrefix());
            this.registerDataFrameRefreshable(climberSensor);
        } else {
            this.climberSensor = null;
        }

        this.mechanismDegreesPerMotorRotation = propertyFactory.createPersistentProperty(
                "MechanismDegreesPerMotorRotation", 3.0);
        this.retractedAngle = propertyFactory.createPersistentProperty("RetractedAngle", Degrees.of(0));
    }

    public boolean isTouchingSensor() {
        // TODO: return true if the climber sensor is triggered
        return false;
    }

    @Override
    public void setPower(Double power) {
        // TODO: set power on both climber motors (remember they might be null!)
    }

    @Override
    public Angle getCurrentValue() {
        // TODO: return the current angle of the climber mechanism
        return Degrees.zero();
    }

    @Override
    public Angle getTargetValue() {
        // TODO: return the target angle of the climber mechanism
        return Degrees.zero();
    }

    @Override
    public void setTargetValue(Angle value) {
        // TODO: remember this target angle in this class for later
    }

    @Override
    public boolean isCalibrated() {
        // TODO: return true if the climber is calibrated, false otherwise
        return false;
    }

    @Override
    protected boolean areTwoTargetsEquivalent(Angle target1, Angle target2) {
        return Math.abs(target1.in(Rotations) - target2.in(Rotations)) < .01;
    }

    @Override
    public void periodic() {
        if (climberMotorLeft != null) {
            climberMotorLeft.periodic();
        }
        if (climberMotorRight != null) {
            climberMotorRight.periodic();
        }

        aKitLog.record("IsCalibrated", isCalibrated());
        aKitLog.record("TargetPosition-Degrees", getTargetValue().in(Degrees));
        aKitLog.record("CurrentPosition-Degrees", getCurrentValue().in(Degrees));
        aKitLog.record("IsTouchingSensor", isTouchingSensor());
    }
}
