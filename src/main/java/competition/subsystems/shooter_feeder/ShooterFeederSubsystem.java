package competition.subsystems.shooter_feeder;

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
public class ShooterFeederSubsystem extends BaseSubsystem {
    public final XCANMotorController shooterFeederMotor;

    @Inject
    public ShooterFeederSubsystem(ElectricalContract electricalContract,
                                  XCANMotorController.XCANMotorControllerFactory motorFactory, PropertyFactory pf) {
        pf.setPrefix(this);

        var defaultPIDProperties = new XCANMotorControllerPIDProperties.Builder()
                .withP(0.0)
                .withI(0.0)
                .withD(0.0)
                .withStaticFeedForward(0)
                .withVelocityFeedForward(0.1)
                .withMinPowerOutput(-1.0)
                .withMaxPowerOutput(1.0)
                .build();

        if (electricalContract.isReady(Hardware.ShooterFeeder)) {
            this.shooterFeederMotor = motorFactory.create(electricalContract.getShooterFeederMotor(),
                    getPrefix(), "ShooterFeederMotorPID", defaultPIDProperties);
            this.shooterFeederMotor.setOpenLoopRampRates(Seconds.of(0.1), Seconds.of(0.1));
            this.shooterFeederMotor.setClosedLoopRampRates(Seconds.of(0.1), Seconds.of(0.1));
            this.registerDataFrameRefreshable(shooterFeederMotor);
        } else {
            this.shooterFeederMotor = null;
        }
    }

    // TODO: add methods to feed fuel into the shooter (fire), eject, and stop

    @Override
    public void periodic() {
        if (shooterFeederMotor != null) {
            shooterFeederMotor.periodic();
        }
    }
}
