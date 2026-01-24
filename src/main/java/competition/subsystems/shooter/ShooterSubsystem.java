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
    public final XCANMotorController shooterMotorLeft;
    public final XCANMotorController shooterMotorMiddle;
    public final XCANMotorController shooterMotorRight;

    public DoubleProperty outputPower;

    @Inject
    public ShooterSubsystem(XCANMotorController.XCANMotorControllerFactory xcanMotorControllerFactory,
                            ElectricalContract eletricalContract, PropertyFactory propertyFactory) {
        propertyFactory.setPrefix(this);
        if (eletricalContract.isShooterReady()) {
            shooterMotorLeft = xcanMotorControllerFactory.create(eletricalContract.getShooterMotor(),
                    getPrefix(),"ShooterMotor");
            shooterMotorMiddle = xcanMotorControllerFactory.create(eletricalContract.getShooterMotor(),
                    getPrefix(),"ShooterMotor");
            shooterMotorRight = xcanMotorControllerFactory.create(eletricalContract.getShooterMotor(),
                    getPrefix(),"ShooterMotor");

            this.registerDataFrameRefreshable(shooterMotorLeft);
            this.registerDataFrameRefreshable(shooterMotorMiddle);
            this.registerDataFrameRefreshable(shooterMotorRight);
        } else {
            this.shooterMotorLeft = null;
            this.shooterMotorMiddle = null;
            this.shooterMotorRight = null;
        }

        this.outputPower = propertyFactory.createPersistentProperty("Output Power", 0.1);
    }

    public void output() {
        if (shooterMotorLeft != null) {
            shooterMotorLeft.setPower(outputPower.get());
        }

        if (shooterMotorMiddle != null) {
            shooterMotorMiddle.setPower(outputPower.get());
        }

        if (shooterMotorRight != null) {
            shooterMotorRight.setPower(outputPower.get());
        }
    }

    public void stop() {
        if (shooterMotorLeft != null) {
            shooterMotorLeft.setPower(0);
        }

        if (shooterMotorMiddle != null) {
            shooterMotorMiddle.setPower(0);
        }

        if (shooterMotorRight != null) {
            shooterMotorRight.setPower(0);
        }
    }

    public void periodic() {
        if (shooterMotorLeft != null) {
            shooterMotorLeft.periodic();
        }

        if (shooterMotorMiddle != null) {
            shooterMotorMiddle.periodic();
        }

        if (shooterMotorRight != null) {
            shooterMotorRight.periodic();
        }
    }
}