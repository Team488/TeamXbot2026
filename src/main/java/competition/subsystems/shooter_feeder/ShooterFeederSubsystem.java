package competition.subsystems.shooter_feeder;

import competition.electrical_contract.ElectricalContract;
import edu.wpi.first.wpilibj2.command.Subsystem;
import xbot.common.command.BaseSubsystem;
import javax.inject.Inject;
import javax.inject.Singleton;
import xbot.common.command.BaseCommand;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;


@Singleton
public class ShooterFeederSubsystem  extends BaseSubsystem {
    public final XCANMotorController shooterFeederMotor;

    public DoubleProperty shooterFeederMotorPower;

    @Inject
    public ShooterFeederSubsystem(ElectricalContract electricalContract,
                                  XCANMotorController.XCANMotorControllerFactory motorFactory, PropertyFactory pf) {

        if (electricalContract.isShooterFeederReady()) {
            this.shooterFeederMotor = motorFactory.create(electricalContract.getShooterFeederMotor(),
                    getPrefix(), "ShooterFeederMotor");
        } else {
            this.shooterFeederMotor = null;
        }
        this.shooterFeederMotorPower = pf.createPersistentProperty("ShooterFeederMotorPower", 0);
        



    }
}