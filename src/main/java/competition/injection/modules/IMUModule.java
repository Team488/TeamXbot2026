package competition.injection.modules;

import javax.inject.Singleton;

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
    public XbotIMU provideIMU(XGyro.XGyroFactory gyroFactory, ElectricalContract contract) {
        return new XGyroIMU(gyroFactory.create(contract.getIMUInfo()));
    }
}
