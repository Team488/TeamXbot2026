package competition.subsystems.hopper_roller;

import competition.electrical_contract.ElectricalContract;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
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
    final DoubleProperty collectPower;
    final DoubleProperty intakePower;
    final AngularVelocityProperty intakeVelocity;
    final AngularVelocityProperty intakePulseVelocity;
    final AngularVelocityProperty collectVelocity;
    final AngularVelocityProperty ejectVelocity;
    final BooleanProperty useVelocityControl;
    final DoubleProperty voltageRampTime;
    final DoubleProperty intakePulseDuration;

    @Inject
    public HopperRollerSubsystem(ElectricalContract electricalContract,
                                 XCANMotorController.XCANMotorControllerFactory motorFactory,
                                 PropertyFactory pf) {

        pf.setPrefix(this);
        this.electricalContract = electricalContract;
        this.voltageRampTime = pf.createPersistentProperty("VoltageRampTime", 0.1);
        if (electricalContract.isHopperRollerReady()) {
            this.hopperRollerMotor = motorFactory.create(
                    electricalContract.getHopperRollerMotor(),
                    getPrefix(),
                    "HopperRollerPID",
                    new XCANMotorControllerPIDProperties.Builder()
                            .withP(0.001)
                            .withVelocityFeedForward(0.008)
                            .withStaticFeedForward(0.05)
                            .build()
            );
            this.hopperRollerMotor.setOpenLoopRampRates(
                    Seconds.of(voltageRampTime.get()),
                    Seconds.of(voltageRampTime.get()));
            this.hopperRollerMotor.setClosedLoopRampRates(
                    Seconds.of(voltageRampTime.get()),
                    Seconds.of(voltageRampTime.get()));
            this.registerDataFrameRefreshable(hopperRollerMotor);
        } else {
            this.hopperRollerMotor = null;
        }

        collectPower = pf.createPersistentProperty("Collect Power", 0.8);
        intakePower = pf.createPersistentProperty("Intake Power", 0.8);
        ejectPower = pf.createPersistentProperty("Eject Power", -0.8);

        useVelocityControl = pf.createPersistentProperty("Use Velocity Control", true);
        intakeVelocity = pf.createPersistentProperty("Intake Velocity", RPM.of(3000));
        intakePulseVelocity = pf.createPersistentProperty("Intake Pulse Velocity", RPM.of(3200));
        collectVelocity = pf.createPersistentProperty("Collect Velocity", RPM.of(3000));
        ejectVelocity = pf.createPersistentProperty("Eject Velocity", RPM.of(-3000));
        intakePulseDuration = pf.createPersistentProperty("Intake Pulse Duration Seconds", 0.5);

        if (hopperRollerMotor != null) {
            hopperRollerMotor.setClosedLoopRampRates(
                    Seconds.of(voltageRampTime.get()),
                    Seconds.of(voltageRampTime.get())
            );
        }
    }

    public void setVelocityTarget(AngularVelocity velocity) {
        if (hopperRollerMotor == null) {
            return;
        }
        hopperRollerMotor.setVelocityTarget(velocity);
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

    public void setCollectPower() {
        if (hopperRollerMotor == null) {
            return;
        }
        if (useVelocityControl.get()) {
            hopperRollerMotor.setVelocityTarget(collectVelocity.get());
        } else {
            hopperRollerMotor.setPower(collectPower.get());
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
                hopperRollerMotor.setOpenLoopRampRates(
                        Seconds.of(voltageRampTime.get()),
                        Seconds.of(voltageRampTime.get())
                );
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

    public Command getCollectCommand() {return new NamedRunCommand(getName() + "-collect", this::setCollectPower, this);}

    public Command getIntakePulseCommand() {
        var commandGroup = new SequentialCommandGroup();
        commandGroup.setName(getName() + "-intake-pulse");
        commandGroup.addCommands(new NamedRunCommand(getName() + "-intake-pulse-low", () -> setVelocityTarget(intakeVelocity.get()), this).withTimeout(intakePulseDuration.get()));
        commandGroup.addCommands(new NamedRunCommand(getName() + "-intake-pulse-high", () -> setVelocityTarget(intakePulseVelocity.get()), this).withTimeout(intakePulseDuration.get()));
        return commandGroup.repeatedly();
    }
}
