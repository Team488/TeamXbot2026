package competition.subsystems.intake_deploy;

import competition.electrical_contract.ElectricalContract;
import competition.subsystems.intake_deploy.commands.CalibrateOffsetDown;
import competition.subsystems.intake_deploy.commands.CalibrateOffsetUp;
import edu.wpi.first.units.measure.Angle;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.properties.AngleProperty;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;
import xbot.common.controls.sensors.XAbsoluteEncoder;
import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.Degree;
import static edu.wpi.first.units.Units.Rotation;

@Singleton
public class IntakeDeploySubsystem extends BaseSubsystem {
    public final XCANMotorController intakeDeployMotor;
    public final XAbsoluteEncoder intakeDeployAbsoluteEncoder;
    public DoubleProperty retractPower;
    public DoubleProperty extendPower;
    public AngleProperty limbRange;
    public Angle offset;
    public boolean isCalibrated = false;

    @Inject
    public IntakeDeploySubsystem(XCANMotorController.XCANMotorControllerFactory xcanMotorControllerFactory,
              ElectricalContract electricalContract, PropertyFactory propertyFactory,
                                 XAbsoluteEncoder.XAbsoluteEncoderFactory xAbsoluteEncoderFactory) {
        propertyFactory.setPrefix(this);
        if (electricalContract.isIntakeDeployReady()) {
            this.intakeDeployMotor = xcanMotorControllerFactory.create(electricalContract.getIntakeDeployMotor(),
                    getPrefix(),"intakeDeploy");
            this.registerDataFrameRefreshable(this.intakeDeployMotor);
        } else {
            this.intakeDeployMotor = null;
        }

        if (electricalContract.isIntakeDeployAbsoluteEncoderReady()) {
            this.intakeDeployAbsoluteEncoder = xAbsoluteEncoderFactory.create(electricalContract.getIntakeDeployAbsoluteEncoderMotor(),
                    getPrefix());
            registerDataFrameRefreshable(intakeDeployAbsoluteEncoder);
        } else {
            this.intakeDeployAbsoluteEncoder = null;
        }

        this.retractPower = propertyFactory.createPersistentProperty("retractPower", -0.1);
        this.extendPower = propertyFactory.createPersistentProperty("extendPower", 0.1);
    }

    public void retract() {
        intakeDeployMotor.setPower(retractPower.get());
    }

    public void extend() {
        intakeDeployMotor.setPower(extendPower.get());
    }

    public void stop() {
        intakeDeployMotor.setPower(0);
    }

    public void periodic() {
        if (intakeDeployMotor != null) {
            intakeDeployMotor.periodic();
        }
    }

    public void calibrateOffsetDown() {
        offset = intakeDeployMotor.getPosition().minus(limbRange.get());
        isCalibrated = true;
    }

    public void calibrateOffsetUp() {
        offset = intakeDeployMotor.getPosition();
        isCalibrated = true;
    }
}
