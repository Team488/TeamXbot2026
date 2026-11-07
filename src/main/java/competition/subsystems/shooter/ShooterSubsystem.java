package competition.subsystems.shooter;

import competition.electrical_contract.ElectricalContract;
import competition.electrical_contract.Hardware;
import edu.wpi.first.units.measure.AngularVelocity;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.controls.actuators.XCANMotorControllerPIDProperties;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

import static edu.wpi.first.units.Units.RPM;
import static edu.wpi.first.units.Units.Seconds;

@Singleton
public class ShooterSubsystem extends BaseSetpointSubsystem<AngularVelocity, Double> {
    public final XCANMotorController leftShooterMotor;
    public final XCANMotorController middleShooterMotor;
    public final XCANMotorController rightShooterMotor;
    public final ElectricalContract electricalContract;

    @Inject
    public ShooterSubsystem(XCANMotorController.XCANMotorControllerFactory xcanMotorControllerFactory,
                            ElectricalContract electricalContract, PropertyFactory propertyFactory) {
        propertyFactory.setPrefix(this);
        this.electricalContract = electricalContract;

        var defaultPIDProperties = new XCANMotorControllerPIDProperties.Builder()
                .withP(0.05)
                .withI(0.0)
                .withD(0.0)
                .withStaticFeedForward(0.02)
                .withVelocityFeedForward(0.01)
                .withMinPowerOutput(-1.0)
                .withMaxPowerOutput(1.0)
                .build();

        if (electricalContract.isReady(Hardware.LeftShooter)) {
            this.leftShooterMotor = xcanMotorControllerFactory.create(electricalContract.getLeftShooterMotor(),
                    getPrefix(), "leftShooterMotor", defaultPIDProperties);
            this.leftShooterMotor.setClosedLoopRampRates(Seconds.of(0.2), Seconds.of(0.2));
            this.registerDataFrameRefreshable(leftShooterMotor);
        } else {
            this.leftShooterMotor = null;
        }

        if (electricalContract.isReady(Hardware.MiddleShooter)) {
            this.middleShooterMotor = xcanMotorControllerFactory.create(electricalContract.getMiddleShooterMotor(),
                    getPrefix(), "middleShooterMotor", defaultPIDProperties);
            this.middleShooterMotor.setClosedLoopRampRates(Seconds.of(0.2), Seconds.of(0.2));
            this.registerDataFrameRefreshable(middleShooterMotor);
        } else {
            this.middleShooterMotor = null;
        }

        if (electricalContract.isReady(Hardware.RightShooter)) {
            this.rightShooterMotor = xcanMotorControllerFactory.create(electricalContract.getRightShooterMotor(),
                    getPrefix(), "rightShooterMotor", defaultPIDProperties);
            this.rightShooterMotor.setClosedLoopRampRates(Seconds.of(0.2), Seconds.of(0.2));
            this.registerDataFrameRefreshable(rightShooterMotor);
        } else {
            this.rightShooterMotor = null;
        }
    }

    /**
     * Convenience method to get all the shooter motors that exist on this robot.
     */
    public List<XCANMotorController> getShooterMotors() {
        var motors = new ArrayList<XCANMotorController>(3);
        if (leftShooterMotor != null) {
            motors.add(leftShooterMotor);
        }
        if (middleShooterMotor != null) {
            motors.add(middleShooterMotor);
        }
        if (rightShooterMotor != null) {
            motors.add(rightShooterMotor);
        }
        return motors;
    }

    @Override
    public AngularVelocity getCurrentValue() {
        // TODO: return the current velocity of the shooter wheels
        return RPM.zero();
    }

    @Override
    public AngularVelocity getTargetValue() {
        // TODO: return the target velocity of the shooter wheels
        return RPM.zero();
    }

    @Override
    public void setTargetValue(AngularVelocity value) {
        // TODO: remember this target velocity in this class for later
    }

    @Override
    public void setPower(Double power) {
        // TODO: set power on all of the shooter motors
    }

    @Override
    public boolean isCalibrated() {
        // TODO: does a shooter wheel need to be calibrated?
        return false;
    }

    @Override
    protected boolean areTwoTargetsEquivalent(AngularVelocity target1, AngularVelocity target2) {
        return Math.abs(target1.in(RPM) - target2.in(RPM)) < 0.00001;
    }

    @Override
    public void periodic() {
        for (var motor : getShooterMotors()) {
            motor.periodic();
        }
        aKitLog.record("ShooterCurrentVelocity-RPM", getCurrentValue().in(RPM));
        aKitLog.record("ShooterTargetVelocity-RPM", getTargetValue().in(RPM));
    }
}
