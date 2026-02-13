package competition.subsystems.shooter_feeder;

import competition.electrical_contract.ElectricalContract;
import xbot.common.command.BaseSubsystem;
import javax.inject.Inject;
import javax.inject.Singleton;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

@Singleton
public class ShooterFeederSubsystem extends BaseSubsystem {
    public final XCANMotorController shooterFeederMotor;

    public DoubleProperty shooterFeederMotorPower;
    public DoubleProperty firePower;

    @Inject
    public ShooterFeederSubsystem(ElectricalContract electricalContract,
                                  XCANMotorController.XCANMotorControllerFactory motorFactory, PropertyFactory pf) {
        pf.setPrefix(this);
        if (electricalContract.isShooterFeederReady()) {
            this.shooterFeederMotor = motorFactory.create(electricalContract.getShooterFeederMotor(),
                    getPrefix(), "ShooterFeederMotorPID");
            this.registerDataFrameRefreshable(shooterFeederMotor);
        } else {
            this.shooterFeederMotor = null;
        }
        this.shooterFeederMotorPower = pf.createPersistentProperty("ShooterFeederMotorPower", 1);
    }

    @Override
    public void periodic() {
        if (shooterFeederMotor != null) {
            shooterFeederMotor.periodic();
        }
    }

    public void fire() {
        if (shooterFeederMotor != null) {
            shooterFeederMotor.setPower(firePower.get());
        }
    }

    public void stop() {
        if (shooterFeederMotor != null) {
            shooterFeederMotor.setPower(0);
        }
    }
}