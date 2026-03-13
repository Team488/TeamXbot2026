package competition.subsystems.collector_intake;

import competition.electrical_contract.ElectricalContract;
import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.properties.AngleProperty;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.Degrees;

@Singleton
public class CollectorSubsystem extends BaseSubsystem {

    public final IntakeDeploySubsystem intakeDeploySubsystem;
    public final ElectricalContract electricalContract;
    public final XCANMotorController collectorMotor;
    final DoubleProperty intakePower;
    final DoubleProperty ejectPower;
    final AngleProperty collectorAngle;


    @Inject
    public CollectorSubsystem(ElectricalContract electricalContract,
                              XCANMotorController.XCANMotorControllerFactory motorFactory,
                              PropertyFactory pf, IntakeDeploySubsystem intakeDeploySubsystem) {

        pf.setPrefix(this);
        this.intakeDeploySubsystem = intakeDeploySubsystem;
        this.electricalContract = electricalContract;
        if (electricalContract.isFuelIntakeMotorReady()) {
            this.collectorMotor = motorFactory.create(
                    electricalContract.getFuelIntakeMotor(),
                    getPrefix(),
                    "FuelIntakePID"
            );
            this.registerDataFrameRefreshable(collectorMotor);
        } else {
            this.collectorMotor = null;
        }

        intakePower = pf.createPersistentProperty("FuelIntakePower", 1);
        ejectPower = pf.createPersistentProperty("FuelEjectPower", -1);
        collectorAngle = pf.createPersistentProperty("CollectorAngle", Degrees.of(90));
    }

    public void intake() {
        this.setPower(intakePower.get());
    }

    public void eject() {
        this.setPower(ejectPower.get());
    }

    public void stop() {
        this.setPower(0.0);
    }

    public void setPower(double power) {
        if (collectorMotor == null) {
            return;
        }

        if (!intakeDeploySubsystem.isCalibrated) {
            return;
        }

        if (intakeDeploySubsystem.getCurrentValue().gt(collectorAngle.get())) {
            collectorMotor.setPower(power);
        }
    }

    @Override
    public void periodic() {
        if (electricalContract.isFuelIntakeMotorReady()) {
            collectorMotor.periodic();
        }
    }
}
