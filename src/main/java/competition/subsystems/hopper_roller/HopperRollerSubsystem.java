package competition.subsystems.hopper_roller;

import competition.electrical_contract.ElectricalContract;
import edu.wpi.first.wpilibj2.command.Command;
import xbot.common.command.BaseSubsystem;
import xbot.common.command.NamedRunCommand;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

public class HopperRollerSubsystem extends BaseSubsystem {

    public HopperRollerSubsystem hopperRollerSubsystem;
    public ElectricalContract electricalContract;
    public XCANMotorController hopperRollerMotor;
    public PropertyFactory pf;
    DoubleProperty forwardPower;
    DoubleProperty reversePower;

    public HopperRollerSubsystem (ElectricalContract electricalContract,
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
            this.registerDataFrameRefreshable(hopperRollerMotor);
        } else {
            this.hopperRollerMotor = null;
        }

        reversePower = pf.createPersistentProperty("Reverse Power", 0.1);
        forwardPower = pf.createPersistentProperty("Forward Power", -0.1);

    }

    public void setForwardPower() {
        if (hopperRollerMotor == null) {
            return;
        }
        hopperRollerMotor.setPower(forwardPower.get());
    }

    public void setReversePower() {
        if (hopperRollerMotor == null) {
            return;
        }
        hopperRollerMotor.setPower(reversePower.get());
    }

    public void stop() {
        if (hopperRollerMotor == null) {
            return;
        }
        hopperRollerMotor.setPower(0);
    }

    public Command getFowardCommand() {
        return new NamedRunCommand(getName() + "-forward", this::setForwardPower, this);
    }

    public Command getReverseCommand() {
        return new NamedRunCommand(getName() + "-reverse", this::setReversePower, this);
    }

    public Command getStopCommand() {
        return new NamedRunCommand(getName() + "-stop", this::stop, this);
    }
}
