package competition.subsystems.shooter;

import competition.electrical_contract.ElectricalContract;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ShooterSubsystem extends BaseSubsystem {
    public final XCANMotorController shooterMotor;

    public DoubleProperty outputPower;

    @Inject
    public ShooterSubsystem(XCANMotorController.XCANMotorControllerFactory xcanMotorControllerFactory,
                            ElectricalContract eletricalContract, PropertyFactory propertyFactory) {
        propertyFactory.setPrefix(this);
        if (eletricalContract.isShooterReady()) {
            shooterMotor = xcanMotorControllerFactory.create(eletricalContract.getShooterMotor(),
                    getPrefix(),"ShooterMotor");
        } else {
            this.shooterMotor = null;
        }

        this.outputPower = propertyFactory.createPersistentProperty("Output Power", 0.1);
    }
    public void output() {
        if (shooterMotor != null) {
            shooterMotor.setPower(outputPower.get());
        }
    }

    public void stop() {
        if (shooterMotor != null) {
            shooterMotor.setPower(0);
        }
    }
    public void periodic() {
        if (shooterMotor != null) {
            shooterMotor.periodic();
        }
    }
}