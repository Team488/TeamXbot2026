package competition.subsystems.shooter;

import competition.electrical_contract.ElectricalContract;
import edu.wpi.first.units.AngularAccelerationUnit;
import edu.wpi.first.units.measure.AngularVelocity;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.controls.actuators.XCANMotorControllerPIDProperties;
import xbot.common.controls.actuators.XSpeedController;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.lang.annotation.Target;

import static edu.wpi.first.units.Units.RPM;

@Singleton
public class ShooterSubsystem extends BaseSetpointSubsystem<AngularVelocity, Double> {
    public final XCANMotorController shooterMotor;
    public ElectricalContract electricalContract;
    public DoubleProperty targetVelocity;
    double rotationAtZero = 0;

    @Inject
    public ShooterSubsystem(XCANMotorController.XCANMotorControllerFactory xcanMotorControllerFactory,
                            ElectricalContract electricalContract, PropertyFactory propertyFactory) {
        propertyFactory.setPrefix(this);
        this.electricalContract = electricalContract;

        if (electricalContract.isShooterReady()) {
            shooterMotor = xcanMotorControllerFactory.create(electricalContract.getShooterMotor(),
                    getPrefix(),"ShooterMotorPID",
                    new XCANMotorControllerPIDProperties(
                            0.1,
                            0.01,
                            0.25,
                            0.0002,
                            0.750,
                            1,
                            0));
            registerDataFrameRefreshable(shooterMotor);
        } else {
            this.shooterMotor = null;
        }

        this.targetVelocity = propertyFactory.createPersistentProperty("Target Velocity", 3000);
    }

    public void runAtTargetVelocity() {
        shooterMotor.setVelocityTarget(RPM.of(targetVelocity.get()));
    }

    public void setTargetVelocity(double velocity) {
        targetVelocity.set(velocity);
        shooterMotor.setVelocityTarget(RPM.of(targetVelocity.get()));
    }

    public void stop() {
        if (shooterMotor != null) {
            shooterMotor.setPower(0);
        }
    }

    public void increaseTargetVelocity() {
        targetVelocity.set(targetVelocity.get() + 25);
        shooterMotor.setVelocityTarget(RPM.of(targetVelocity.get()));
    }

    public void decreaseTargetVelocity() {
        targetVelocity.set(targetVelocity.get() - 25);
        shooterMotor.setVelocityTarget(RPM.of(targetVelocity.get()));
    }

    public void periodic() {
        if (shooterMotor != null) {
            shooterMotor.periodic();
        }
    }

    @Override
    public AngularVelocity getCurrentValue() {
        if (electricalContract.isShooterReady()) {
            return shooterMotor.getVelocity();
        }
        return RPM.zero(); //rpm = rotation per minute
    }

    @Override
    public AngularVelocity getTargetValue() {
        return RPM.of(targetVelocity.get());
        //targetVelocity = double property - .get() makes it into a double
    }

    @Override
    public void setTargetValue(AngularVelocity value) {

    }

    @Override
    public void setPower(Double power) {

    }

    @Override
    public boolean isCalibrated() {
        return false;
    }

    @Override
    protected boolean areTwoTargetsEquivalent(AngularVelocity target1, AngularVelocity target2) {
        return false;
    }
}