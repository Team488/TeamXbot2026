package competition.injection.components;

import javax.inject.Singleton;

import competition.injection.modules.CommonModule;
import competition.injection.modules.SimulatedRobotModule;
import dagger.Component;
import xbot.common.injection.modules.MockDevicesModule;
import xbot.common.injection.modules.RealControlsModule;
import xbot.common.injection.modules.SimulationModule;

@Singleton
@Component(modules = { SimulationModule.class, MockDevicesModule.class, RealControlsModule.class,
        SimulatedRobotModule.class, CommonModule.class })
public abstract class SimulationComponent extends BaseRobotComponent {
    
}
