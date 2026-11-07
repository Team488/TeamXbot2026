package competition.subsystems.hopper_roller;

import competition.electrical_contract.ElectricalContract;
import competition.electrical_contract.Hardware;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.controls.actuators.XCANMotorControllerPIDProperties;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.Seconds;

@Singleton
public class HopperRollerSubsystem extends BaseSubsystem {

    public final ElectricalContract electricalContract;
    public final XCANMotorController hopperRollerMotor;

    @Inject
    public HopperRollerSubsystem(ElectricalContract electricalContract,
                                 XCANMotorController.XCANMotorControllerFactory motorFactory,
                                 PropertyFactory pf) {
        pf.setPrefix(this);
        this.electricalContract = electricalContract;

        if (electricalContract.isReady(Hardware.HopperRoller)) {
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
            this.hopperRollerMotor.setOpenLoopRampRates(Seconds.of(0.1), Seconds.of(0.1));
            this.hopperRollerMotor.setClosedLoopRampRates(Seconds.of(0.1), Seconds.of(0.1));
            this.registerDataFrameRefreshable(hopperRollerMotor);
        } else {
            this.hopperRollerMotor = null;
        }
    }

    // TODO: add methods to run the hopper roller forward (toward the shooter), in reverse (eject), and stop

    @Override
    public void periodic() {
        if (hopperRollerMotor != null) {
            hopperRollerMotor.periodic();
        }
    }
}
