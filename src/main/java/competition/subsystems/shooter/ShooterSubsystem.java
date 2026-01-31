package competition.subsystems.shooter;

import competition.electrical_contract.ElectricalContract;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.controls.actuators.XCANMotorControllerPIDProperties;
import xbot.common.controls.actuators.XCANMotorController.MotorPidMode;
import xbot.common.controls.sensors.XDigitalInput;
import xbot.common.injection.electrical_contract.DeviceInfo;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.RPM;

@Singleton
public class ShooterSubsystem extends BaseSubsystem {
    public final XCANMotorController leftShooterMotor;
    public final XCANMotorController middleShooterMotor;
    public final XCANMotorController rightShooterMotor;
    public final XDigitalInput shooterBeamBreak;

    public final DoubleProperty targetVelocity;
    public final DoubleProperty activeShotFeedForward;

    @Inject
    public ShooterSubsystem(XCANMotorController.XCANMotorControllerFactory xcanMotorControllerFactory,
                            ElectricalContract eletricalContract,
                            PropertyFactory propertyFactory, XDigitalInput.XDigitalInputFactory digitalInputFactory) {

        propertyFactory.setPrefix(this);
        // TODO: Put in contract
        shooterBeamBreak = digitalInputFactory.create(new DeviceInfo("BeamBreak", 1), this.getPrefix());
        this.dataFrameRefreshables.add(shooterBeamBreak);

        var defaultPIDProperties = new XCANMotorControllerPIDProperties(
                0.1,
                0.01,
                0.25,
                0.0002,
                0.750,
                1,
                0);

        
        
        this.activeShotFeedForward = propertyFactory.createPersistentProperty("Active Shot Feed Forward", 0.0);
        
        if (eletricalContract.isLeftShooterReady()) {
            this.leftShooterMotor = xcanMotorControllerFactory.create(eletricalContract.getLeftShooterMotor(),
                    getPrefix(), "ShooterMotor", defaultPIDProperties);
            this.registerDataFrameRefreshable(leftShooterMotor);
        } else {
            this.leftShooterMotor = null;
        }

        if (eletricalContract.isMiddleShooterReady()) {
            this.middleShooterMotor = xcanMotorControllerFactory.create(eletricalContract.getMiddleShooterMotor(),
                    getPrefix(), "ShooterMotor", defaultPIDProperties);
            this.registerDataFrameRefreshable(middleShooterMotor);
        } else {
            this.middleShooterMotor = null;
        }

        if (eletricalContract.isRightShooterReady()) {
            this.rightShooterMotor = xcanMotorControllerFactory.create(eletricalContract.getRightShooterMotor(),
                    getPrefix(), "ShooterMotor", defaultPIDProperties);
            this.registerDataFrameRefreshable(rightShooterMotor);
        } else {
            this.rightShooterMotor = null;
        }

        this.targetVelocity = propertyFactory.createPersistentProperty("Target Velocity", 3000);
    }

    public void stop() {
        if (leftShooterMotor != null) {
            leftShooterMotor.setPower(0);
        }

        if (middleShooterMotor != null) {
            middleShooterMotor.setPower(0);
        }

        if (rightShooterMotor != null) {
            rightShooterMotor.setPower(0);
        }
    }

    public void increaseTargetVelocity() {
        targetVelocity.set(targetVelocity.get() + 25);
    }

    public void decreaseTargetVelocity() {
        targetVelocity.set(targetVelocity.get() - 25);

    }

    public void setTargetVelocity(double velocity) {
        targetVelocity.set(velocity);
    }

    public void runAtTargetVelocity() {
        if (leftShooterMotor != null) {
            if(shooterBeamBreak.get()) {
                leftShooterMotor.setVelocityTarget(RPM.of(targetVelocity.get()), MotorPidMode.DutyCycle, 1);
            } else {
                leftShooterMotor.setVelocityTarget(RPM.of(targetVelocity.get()));
            }
        }

        if (middleShooterMotor != null) {
            middleShooterMotor.setVelocityTarget(RPM.of(targetVelocity.get()));
        }

        if (rightShooterMotor != null) {
            rightShooterMotor.setVelocityTarget(RPM.of(targetVelocity.get()));
        }
    }

    public void periodic() {
        if (leftShooterMotor != null) {
            leftShooterMotor.periodic();
            // Manually sync the slot 1 pid properties
            leftShooterMotor.setPidDirectly(0, 0, 0, activeShotFeedForward.get(), 0, 1);
        }

        if (middleShooterMotor != null) {
            middleShooterMotor.periodic();
        }

        if (rightShooterMotor != null) {
            rightShooterMotor.periodic();
        }
    }
}