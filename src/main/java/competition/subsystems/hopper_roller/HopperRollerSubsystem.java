package competition.subsystems.hopper_roller;

import competition.electrical_contract.ElectricalContract;
import edu.wpi.first.wpilibj2.command.Command;
import xbot.common.command.BaseSubsystem;
import xbot.common.command.NamedRunCommand;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HopperRollerSubsystem extends BaseSubsystem {

    public final ElectricalContract electricalContract;
    public final XCANMotorController hopperRollerMotor;
    final DoubleProperty ejectPower;
    final DoubleProperty intakePower;

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
                    "HopperRollerPID"
            );
        } else {
            this.hopperRollerMotor = null;
        }

        intakePower = pf.createPersistentProperty("Intake Power", 0.1);
        ejectPower = pf.createPersistentProperty("Eject Power", -0.1);

    }

    public void setEjectPower() {
        if (hopperRollerMotor == null) {
            return;
        }
        hopperRollerMotor.setPower(ejectPower.get());
    }

    public void setIntakePower() {
        if (hopperRollerMotor == null) {
            return;
        }
        hopperRollerMotor.setPower(intakePower.get());
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
        return new NamedRunCommand(getName() + "-intake", this::setIntakePower, this);
    }

    public Command getStopCommand() {
        return new NamedRunCommand(getName() + "-stop", this::stop, this);
    }
}
