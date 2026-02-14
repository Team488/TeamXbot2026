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
    public DoubleProperty pointOnePower;
    public DoubleProperty pointTwoPower;
    public DoubleProperty pointThreePower;
    public DoubleProperty pointFourPower;

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
        this.pointOnePower = pf.createPersistentProperty("one", 0.4); //to do change value
        this.pointTwoPower = pf.createPersistentProperty("Two", 0.6); //to do change value
        this.pointThreePower = pf.createPersistentProperty("Three", 0.8); //to do change value
        this.pointFourPower = pf.createPersistentProperty("Four", 1.0); //to do change value
    }

    @Override
    public void periodic() {
        if (shooterFeederMotor != null) {
            shooterFeederMotor.periodic();
        }
    }

}