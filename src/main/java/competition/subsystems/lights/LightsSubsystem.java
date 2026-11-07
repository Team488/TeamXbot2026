package competition.subsystems.lights;

import competition.electrical_contract.ElectricalContract;
import competition.electrical_contract.Hardware;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANLightController;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LightsSubsystem extends BaseSubsystem {
    public final XCANLightController lights;

    @Inject
    public LightsSubsystem(XCANLightController.XCANLightControllerFactory lightsFactory,
                           ElectricalContract electricalContract) {
        if (electricalContract.isReady(Hardware.Lights)) {
            this.lights = lightsFactory.create(
                    electricalContract.getLightControllerInfo()
            );
        } else {
            this.lights = null;
        }
    }

    @Override
    public void periodic() {
        super.periodic();
        if (lights == null) {
            return;
        }

        // TODO: set the lights based on what the robot is doing.
        // Example: lights.larson(0, Hertz.of(25), Color.kBlue, LarsonBounceValue.Front);
    }
}
