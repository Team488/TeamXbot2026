package competition.injection.components;

import javax.inject.Singleton;

import competition.injection.modules.CommonModule;
import competition.injection.modules.CompetitionModule;
import competition.injection.modules.IMUModule;
import dagger.Component;
import xbot.common.injection.modules.RealControlsModule;
import xbot.common.injection.modules.RealDevicesModule;
import xbot.common.injection.modules.RobotModule;

@Singleton
@Component(modules = { RobotModule.class, RealDevicesModule.class, RealControlsModule.class,
        CompetitionModule.class, CommonModule.class, IMUModule.class})
public abstract class RobotComponent extends BaseRobotComponent {

}
