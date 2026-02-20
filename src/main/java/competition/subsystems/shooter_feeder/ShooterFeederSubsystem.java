package competition.subsystems.shooter_feeder;

import competition.electrical_contract.ElectricalContract;
import xbot.common.command.BaseSubsystem;
import javax.inject.Inject;
import javax.inject.Singleton;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.controls.actuators.XCANMotorControllerPIDProperties;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import static edu.wpi.first.units.Units.RPM;

@Singleton
public class ShooterFeederSubsystem extends BaseSubsystem {
    public final XCANMotorController shooterFeederMotor;

    public final DoubleProperty shooterFeederMotorPower;
    public final DoubleProperty firePower;
    public final DoubleProperty shooterFeederVelocity;

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

        if (electricalContract.isShooterFeederReady()) {
            this.shooterFeederMotor = motorFactory.create(electricalContract.getShooterFeederMotor(),
                    getPrefix(), "ShooterFeederMotorPID", defaultPIDProperties);
            this.registerDataFrameRefreshable(shooterFeederMotor);
        } else {
            this.shooterFeederMotor = null;
        }
        this.shooterFeederMotorPower = pf.createPersistentProperty("ShooterFeederMotorPower", 1);
        this.firePower = pf.createPersistentProperty("firePower", 1);
        this.shooterFeederVelocity = pf.createPersistentProperty("RPMShooterFeederVelocity", 1);
    }

    @Override
    public void periodic() {
        if (shooterFeederMotor != null) {
            shooterFeederMotor.periodic();
        }
    }

    public void stop() {
        shooterFeederMotor.setPower(0);
    }

    public void fire() {
        shooterFeederMotor.setPower(shooterFeederMotorPower.get());
    }

    public void eject() {
        shooterFeederMotor.setPower(-1 * shooterFeederMotorPower.get());
    }

    public void fireVelocity () {
        if (shooterFeederMotor != null) {
            shooterFeederMotor.setVelocityTarget(RPM.of(shooterFeederVelocity.get()));
        }
    }
}