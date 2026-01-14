package competition.injection.modules;

import javax.inject.Singleton;

import competition.electrical_contract.CompetitionContract;
import competition.electrical_contract.ElectricalContract;
import competition.simulation.BaseSimulator;
import competition.simulation.NoopSimulator;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import dagger.Binds;
import dagger.Module;
import xbot.common.injection.electrical_contract.XCameraElectricalContract;
import xbot.common.injection.electrical_contract.XSwerveDriveElectricalContract;
import xbot.common.subsystems.drive.BaseDriveSubsystem;
import xbot.common.subsystems.drive.BaseSwerveDriveSubsystem;
import xbot.common.subsystems.pose.BasePoseSubsystem;

@Module
public abstract class CompetitionModule {
    @Binds
    @Singleton
    public abstract ElectricalContract getElectricalContract(CompetitionContract impl);

    @Binds
    @Singleton
    public abstract XSwerveDriveElectricalContract getSwerveContract(ElectricalContract impl);

    @Binds
    @Singleton
    public abstract BasePoseSubsystem getPoseSubsystem(PoseSubsystem impl);

    @Binds
    @Singleton
    public abstract BaseSwerveDriveSubsystem getSwerveDriveSubsystem(DriveSubsystem impl);

    @Binds
    @Singleton
    public abstract BaseDriveSubsystem getDriveSubsystem(BaseSwerveDriveSubsystem impl);

    @Binds
    @Singleton
    public abstract BaseSimulator getSimulator(NoopSimulator impl);
}
