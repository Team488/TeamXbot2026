package competition.injection.modules;

import javax.inject.Singleton;

import competition.subsystems.pose.SystemCoreIMU;
import dagger.Module;
import dagger.Provides;
import competition.subsystems.pose.XbotIMU;
import competition.subsystems.pose.XGyroIMU;
import xbot.common.controls.sensors.XGyro;
import competition.electrical_contract.ElectricalContract;

@Module
public class IMUModule {

    @Provides
    @Singleton
    public XbotIMU provideIMU(ElectricalContract contract) {
        return new SystemCoreIMU();
    }
}
