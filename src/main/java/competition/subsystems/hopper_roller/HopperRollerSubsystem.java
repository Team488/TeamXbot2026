package competition.subsystems.hopper_roller;

import competition.electrical_contract.ElectricalContract;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.wpilibj2.command.Command;
import xbot.common.command.BaseSubsystem;
import xbot.common.command.NamedRunCommand;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.properties.AngularVelocityProperty;
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
    final AngularVelocityProperty intakeVelocity;
    final DoubleProperty intakePower;
    public final DoubleProperty voltageRampTime;

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
                    "HopperRollerPID"
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

        intakeVelocity = pf.createPersistentProperty("Intake Velocity", RPM.of(0.8));
        intakePower = pf.createPersistentProperty("Intake Velocity", 0.8);
        ejectPower = pf.createPersistentProperty("Eject Power", -0.8);
    }

    public void setEjectPower() {
        if (hopperRollerMotor == null) {
            return;
        }
        hopperRollerMotor.setPower(ejectPower.get());
    }

    public void setIntakeVelocity() {
        if (hopperRollerMotor == null) {
            return;
        }
        hopperRollerMotor.setVelocityTarget(intakeVelocity.get());
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
        }
    }

    public Command getEjectCommand() {
        return new NamedRunCommand(getName() + "-eject", this::setEjectPower, this);
    }

    public Command getIntakeCommand() {
        return new NamedRunCommand(getName() + "-intake", this::setIntakeVelocity, this);
    }

    public Command getStopCommand() {
        return new NamedRunCommand(getName() + "-stop", this::stop, this);
    }
}
