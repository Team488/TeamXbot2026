package competition.subsystems.hood;

import competition.electrical_contract.ElectricalContract;

import edu.wpi.first.units.measure.Angle;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.controls.actuators.XCANMotorControllerPIDProperties;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HoodSubsystem extends BaseSetpointSubsystem<Angle, Double> {
    public final XCANMotorController hoodMotor;

    public DoubleProperty openPower;
    public DoubleProperty closePower;
    @Inject
    public HoodSubsystem(XCANMotorController.XCANMotorControllerFactory xcanMotorControllerFactory,
                         ElectricalContract electricalContract, PropertyFactory propertyFactory) {

        propertyFactory.setPrefix(this);

        if (electricalContract.isHoodReady()) {
            this.hoodMotor = xcanMotorControllerFactory.create(electricalContract.getHoodMotor(),
                    this.getPrefix(),
                    "HoodMotorPID",
                    new XCANMotorControllerPIDProperties());
        } else {
            this.hoodMotor = null;
        };
        openPower = propertyFactory.createPersistentProperty("open hood", 0.1);
        closePower = propertyFactory.createPersistentProperty("close hood", -0.1);

    };

    public void openHood() {
        if (hoodMotor != null) {
            hoodMotor.setPower(openPower.get());
        }
    }
    public void closeHood() {
        if (hoodMotor != null) {
            hoodMotor.setPower(openPower.get());
        }
    }
    public void stopHood() {
        if (hoodMotor != null) {
            hoodMotor.setPower(0);
        }
    }

    @Override
    public Angle getCurrentValue() {
        return null;
    }

    @Override
    public Angle getTargetValue() {
        return null;
    }

    @Override
    public void setTargetValue(Angle value) {

    }

    @Override
    public void setPower(Double power) {

    }

    @Override
    public boolean isCalibrated() {
        return false;
    }

    @Override
    protected boolean areTwoTargetsEquivalent(Angle target1, Angle target2) {
        return false;
    }
}
