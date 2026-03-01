package competition.subsystems.climber;

import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkBase;
import com.revrobotics.spark.SparkLowLevel;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import competition.electrical_contract.ElectricalContract;
import competition.operator_interface.OperatorCommandMap;
import competition.operator_interface.OperatorInterface;
import competition.subsystems.climber.commands.ClimberExtendCommand;
import competition.subsystems.climber.commands.ClimberRetractCommand;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.trajectory.constraint.MaxVelocityConstraint;
import edu.wpi.first.units.AngularAccelerationUnit;
import edu.wpi.first.units.measure.Acceleration;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.MutAngle;
import edu.wpi.first.units.measure.Velocity;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.command.NamedRunCommand;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.controls.actuators.XCANMotorControllerPIDProperties;
import xbot.common.controls.sensors.XXboxController;
import xbot.common.math.PID;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.RPM;
import static edu.wpi.first.units.Units.Rotations;

@Singleton
public class ClimberSubsystem extends BaseSetpointSubsystem <Angle, Double> {

    public final XCANMotorController climberMotorLeft;
    public final XCANMotorController climberMotorRight;
    private final DoubleProperty mechanismDegreesPerMotorRotation;
    public final DoubleProperty manualControlPower;
    public DoubleProperty extendPower;
    public DoubleProperty retractPower;
    public ClimberState climberState;
    public Angle motorOffset = Degrees.zero();
    private boolean isCalibrated;


    private final MutAngle mechanismTargetAngle = Degrees.mutable(0);

    public enum ClimberState {
        EXTENDING,
        RETRACTING,
        STOPPED
    }

    @Inject
    public ClimberSubsystem(XCANMotorController.XCANMotorControllerFactory motorFactory,
                            ElectricalContract electricalContract, PropertyFactory propertyFactory) {
        propertyFactory.setPrefix(this);
        if (electricalContract.isClimberLeftReady() && electricalContract.isClimberRightReady()) {
            this.climberMotorLeft = motorFactory.create(
                    electricalContract.getClimberMotorLeft(),
                    getPrefix(), "ClimberMotorPID", new XCANMotorControllerPIDProperties(
                            0,
                            0,
                            0
                    ));
            this.climberMotorRight = motorFactory.create(
                    electricalContract.getClimberMotorRight(),
                    getPrefix(), "ClimberMotorPID", new XCANMotorControllerPIDProperties(
                            0,
                            0,
                            0
                    ));
            this.registerDataFrameRefreshable(climberMotorLeft);
            this.registerDataFrameRefreshable(climberMotorRight);
        } else {
            this.climberMotorLeft = null;
            this.climberMotorRight = null;
        }

        this.mechanismDegreesPerMotorRotation = propertyFactory.createPersistentProperty("MechanismDegreesPerMotorRotation", 0);
        this.manualControlPower = propertyFactory.createPersistentProperty("ManualControlPower", 0.1);
        // TODO: find degrees per rotation
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
        if (climberMotorLeft != null) {
            climberMotorLeft.periodic();
        }
        if (climberMotorRight != null) {
            climberMotorRight.periodic();
        }

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
        return mechanismTargetAngle.copy();
    }

    @Override
    public void setTargetValue(Angle angle) {
        mechanismTargetAngle.mut_replace(angle);
    }

    @Override
    public void setPower(Double power) {
    }

    @Override
    public boolean isCalibrated() {
        return isCalibrated;
    }

    @Override
    protected boolean areTwoTargetsEquivalent(Angle target1, Angle target2) {
        return Math.abs(target1.in(Rotations) - target2.in(Rotations)) < .01;
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

//        if (ClimberRetractCommand == XXboxController.XboxButton.LeftBumper(climberState) == ) {
//
//        }
    }

    TrapezoidProfile.Constraints constraints = new TrapezoidProfile.Constraints(1.0,0.2);

    public void climberSmoothAcceleration () {
        if (XXboxController.XboxButton.LeftBumper.equals(true)) {
            double maxVelocityConstraint = 1.0;
            double maxAccelerationConstraint = 0.2;
        }
    }

    public void unhookFromTower () {
        //set a height of where you want to unhook
        //use trapezoidProfile to slowly rotate the degrees to a safe angle

        //safe landing on (0.0)
    }

    public final Command getCalibrateOffsetRetractCommand() {
        return new NamedRunCommand( getName() + "-calibrate", this::calibrateOffsetRetracted);
    }
}