package competition.subsystems.hopper_roller;

import competition.electrical_contract.ElectricalContract;
import edu.wpi.first.wpilibj2.command.Command;
import xbot.common.command.BaseSubsystem;
import xbot.common.command.NamedRunCommand;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.controls.actuators.XCANMotorControllerPIDProperties;
import xbot.common.properties.AngularVelocityProperty;
import xbot.common.properties.BooleanProperty;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.RPM;
import static edu.wpi.first.units.Units.Seconds;

@Singleton
public class HopperRollerSubsystem extends BaseSubsystem {

    public final ElectricalContract electricalContract;
    public final XCANMotorController hopperRollerMotor;
    final DoubleProperty ejectPower;
    final DoubleProperty intakePower;
    final AngularVelocityProperty intakeVelocity;
    final AngularVelocityProperty ejectVelocity;
    final BooleanProperty useVelocityControl;
    final DoubleProperty voltageRampTime;

    @Inject
    public HopperRollerSubsystem(ElectricalContract electricalContract,
                                 XCANMotorController.XCANMotorControllerFactory motorFactory,
                                 PropertyFactory pf) {

        pf.setPrefix(this);
        this.electricalContract = electricalContract;
        if (electricalContract.isHopperRollerReady()) {
            this.hopperRollerMotor = motorFactory.create(
                    electricalContract.getHopperRollerMotor(),
                    getPrefix(),
                    "HopperRollerPID",
                    new XCANMotorControllerPIDProperties.Builder()
                            .withP(0.0)
                            .withVelocityFeedForward(0.01)
                            .build()
            );
            this.registerDataFrameRefreshable(hopperRollerMotor);
        } else {
            this.hopperRollerMotor = null;
        }

        intakePower = pf.createPersistentProperty("Intake Power", 0.8);
        ejectPower = pf.createPersistentProperty("Eject Power", -0.8);

        useVelocityControl = pf.createPersistentProperty("Use Velocity Control", false);
        intakeVelocity = pf.createPersistentProperty("Intake Velocity", RPM.of(3000));
        ejectVelocity = pf.createPersistentProperty("Eject Velocity", RPM.of(-3000));

        voltageRampTime = pf.createPersistentProperty("Voltage Ramp Time Seconds", 0.2);

        if (hopperRollerMotor != null) {
            hopperRollerMotor.setClosedLoopRampRates(
                    Seconds.of(voltageRampTime.get()),
                    Seconds.of(voltageRampTime.get())
            );
        }
    }

    public void setEjectPower() {
        if (hopperRollerMotor == null) {
            return;
        }
        if (useVelocityControl.get()) {
            hopperRollerMotor.setVelocityTarget(ejectVelocity.get());
        } else {
            hopperRollerMotor.setPower(ejectPower.get());
        }
    }

    public void setIntakePower() {
        if (hopperRollerMotor == null) {
            return;
        }
        if (useVelocityControl.get()) {
            hopperRollerMotor.setVelocityTarget(intakeVelocity.get());
        } else {
            hopperRollerMotor.setPower(intakePower.get());
        }
    }

    public void stop() {
        if (hopperRollerMotor == null) {
            return;
        }
        hopperRollerMotor.setPower(0);
    }

    @Override
    public void periodic() {
        if (hopperRollerMotor != null) {
            hopperRollerMotor.periodic();

            if (voltageRampTime.hasChangedSinceLastCheck()) {
                hopperRollerMotor.setClosedLoopRampRates(
                        Seconds.of(voltageRampTime.get()),
                        Seconds.of(voltageRampTime.get())
                );
            }
        }
    }

    public Command getEjectCommand() {
        return new NamedRunCommand(getName() + "-eject", this::setEjectPower, this);
    }

    public Command getIntakeCommand() {
        return new NamedRunCommand(getName() + "-intake", this::setIntakePower, this);
    }

    public Command getStopCommand() {
        return new NamedRunCommand(getName() + "-stop", this::stop, this);
    }
}
