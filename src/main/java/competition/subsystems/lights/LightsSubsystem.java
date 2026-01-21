package competition.subsystems.lights;


import xbot.common.controls.actuators.XCANLightController;
import xbot.common.injection.electrical_contract.CANBusId;
import xbot.common.injection.electrical_contract.CANLightControllerInfo;
import xbot.common.injection.electrical_contract.LightControllerType;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LightsSubsystem {
    @Inject
    public LightsSubsystem(XCANLightController.XCANLightControllerFactory lightsFactory) {
    XCANLightController lights2 =  lightsFactory.create(new CANLightControllerInfo("Lights", LightControllerType.Candle, CANBusId.RIO, 100));
    
    }
}
