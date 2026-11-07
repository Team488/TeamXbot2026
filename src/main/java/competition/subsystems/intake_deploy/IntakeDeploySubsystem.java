package competition.subsystems.intake_deploy;

import competition.electrical_contract.ElectricalContract;
import competition.electrical_contract.Hardware;
import edu.wpi.first.units.measure.Angle;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.controls.actuators.XCANMotorControllerPIDProperties;
import xbot.common.controls.sensors.XAbsoluteEncoder;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.RotationsPerSecond;
import static edu.wpi.first.units.Units.Second;
import static edu.wpi.first.units.Units.Seconds;

@Singleton
public class IntakeDeploySubsystem extends BaseSetpointSubsystem<Angle, Double> {
    public final XCANMotorController intakeDeployMotor;
    public final XAbsoluteEncoder intakeDeployEncoder;

    // The angle (in degrees) of the intake when it is fully extended / deployed
    public final DoubleProperty extendedPosition;
    // The angle (in degrees) of the intake when it is fully retracted
    public final DoubleProperty retractedPosition;
    // How many degrees the intake moves for each rotation of the motor
    public final DoubleProperty mechanismDegreePerMotorRotation;

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

        if (electricalContract.isReady(Hardware.IntakeDeploy)) {
            this.intakeDeployMotor = xcanMotorControllerFactory.create(electricalContract.getIntakeDeployMotor(),
                    getPrefix(), "IntakeDeployPID", defaultPIDProperties);
            this.intakeDeployMotor.setOpenLoopRampRates(Seconds.of(0.1), Seconds.of(0.1));
            this.intakeDeployMotor.setClosedLoopRampRates(Seconds.of(0.1), Seconds.of(0.1));
            // Settings for the motor's built-in "trapezoidal" motion profile, used by
            // setPositionTarget(..., MotorPidMode.TrapezoidalVoltage)
            this.intakeDeployMotor.setTrapezoidalProfileMaxVelocity(RotationsPerSecond.of(200));
            this.intakeDeployMotor.setTrapezoidalProfileAcceleration(RotationsPerSecond.per(Second).of(200));
            this.registerDataFrameRefreshable(this.intakeDeployMotor);
        } else {
            this.intakeDeployMotor = null;
        }

        if (electricalContract.isReady(Hardware.IntakeDeployEncoder)) {
            // On the real robot this CANCoder is also wired to the motor as a "remote sensor", so
            // motor position targets are in mechanism degrees rather than motor rotations.
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
        this.mechanismDegreePerMotorRotation = propertyFactory.createPersistentProperty(
                "MechanismDegreePerMotorRotation", 360);
    }

    @Override
    public Angle getCurrentValue() {
        // TODO: return the current angle of the intake (hint: use the absolute encoder)
        return Degrees.zero();
    }

    @Override
    public Angle getTargetValue() {
        // TODO: return the target angle of the intake
        return Degrees.zero();
    }

    @Override
    public void setTargetValue(Angle value) {
        // TODO: remember this target angle in this class for later
    }

    @Override
    public void setPower(Double power) {
        // TODO: set power on the intake deploy motor
    }

    @Override
    public boolean isCalibrated() {
        // TODO: return true if the intake deploy is calibrated, false otherwise
        return false;
    }

    @Override
    protected boolean areTwoTargetsEquivalent(Angle target1, Angle target2) {
        return Math.abs(target1.in(Degrees) - target2.in(Degrees)) < 0.001;
    }

    @Override
    public void periodic() {
        if (intakeDeployMotor != null) {
            intakeDeployMotor.periodic();
        }

        aKitLog.record("CurrentPosition", getCurrentValue().in(Degrees));
        aKitLog.record("TargetPosition", getTargetValue().in(Degrees));
    }
}
