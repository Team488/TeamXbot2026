package competition.subsystems.shooter;

import competition.electrical_contract.ElectricalContract;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.RPM;

@Singleton
public class ShooterSubsystem extends BaseSubsystem {
    public final XCANMotorController leftShooterMotor;
    public final XCANMotorController middleshooterMotor;
    public final XCANMotorController rightshooterMotor;

    public DoubleProperty outputPower;
    public DoubleProperty targetVelocity;

    @Inject
    public ShooterSubsystem(XCANMotorController.XCANMotorControllerFactory xcanMotorControllerFactory,
                            XCANMotorController leftShooterMotor, XCANMotorController middleshooterMotor,
                            XCANMotorController rightshooterMotor, ElectricalContract eletricalContract,
                            PropertyFactory propertyFactory) {

        propertyFactory.setPrefix(this);
        if (eletricalContract.isLeftShooterReady()) {
            this.leftShooterMotor = xcanMotorControllerFactory.create(eletricalContract.getLeftShooterMotor(),
                    getPrefix(), "ShooterMotor");
            this.registerDataFrameRefreshable(leftShooterMotor);
        } else {
            this.leftShooterMotor = null;
        }

        if (eletricalContract.isMiddleShooterReady()) {
            this.middleshooterMotor = xcanMotorControllerFactory.create(eletricalContract.getMiddleShooterMotor(),
                    getPrefix(), "ShooterMotor");
            this.registerDataFrameRefreshable(middleshooterMotor);
        } else {
            this.middleshooterMotor = null;
        }

        if (eletricalContract.isRightShooterReady()) {
            this.rightshooterMotor = xcanMotorControllerFactory.create(eletricalContract.getRightShooterMotor(),
                    getPrefix(), "ShooterMotor");
            this.registerDataFrameRefreshable(rightshooterMotor);
        } else {
            this.rightshooterMotor = null;
        }

        this.outputPower = propertyFactory.createPersistentProperty("Output Power", 0.1);
        this.targetVelocity = propertyFactory.createPersistentProperty("Target Velocity", 3000);
    }


    public void output() {
        if (leftShooterMotor != null) {
            leftShooterMotor.setPower(outputPower.get());
        }

        if (middleshooterMotor != null) {
            middleshooterMotor.setPower(outputPower.get());
        }

        if (rightshooterMotor != null) {
            rightshooterMotor.setPower(outputPower.get());
        }
    }

    public void stop() {
        if (leftShooterMotor != null) {
            leftShooterMotor.setPower(0);
        }

        if (middleshooterMotor != null) {
            middleshooterMotor.setPower(0);
        }

        if (rightshooterMotor != null) {
            rightshooterMotor.setPower(0);
        }
    }

    public void increaseTargetVelocity() {
        targetVelocity.set(targetVelocity.get() + 25);
    }

    public void decreaseTargetVelocity() {
        targetVelocity.set(targetVelocity.get() - 25);
    }

    public void periodic() {
        if (leftShooterMotor != null) {
            leftShooterMotor.periodic();
            leftShooterMotor.setVelocityTarget(RPM.of(targetVelocity.get()));
        }

        if (middleshooterMotor != null) {
            middleshooterMotor.periodic();
            middleshooterMotor.setVelocityTarget(RPM.of(targetVelocity.get()));
        }

        if (rightshooterMotor != null) {
            rightshooterMotor.periodic();
            rightshooterMotor.setVelocityTarget(RPM.of(targetVelocity.get()));
        }
    }
}