package competition.subsystems.lights;

import com.ctre.phoenix6.signals.LarsonBounceValue;
import competition.electrical_contract.ElectricalContract;
import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.util.Color;
import org.apache.logging.log4j.core.pattern.AbstractStyleNameConverter;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANLightController;
import competition.subsystems.voltage_alert.VoltageMonitorSubsystem;
import edu.wpi.first.wpilibj.DriverStation.Alliance;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.Hertz;

@Singleton
public class LightsSubsystem extends BaseSubsystem {
    public final XCANLightController lights;
    public IntakeDeploySubsystem intakeDeploy;
    public HoodSubsystem hoodSubsystem;
    public VoltageMonitorSubsystem voltageMonitor;

    @Inject
    public LightsSubsystem(XCANLightController.XCANLightControllerFactory lightsFactory,
                           ElectricalContract electricalContract,
                           IntakeDeploySubsystem intakeDeploy,
                           HoodSubsystem hoodSubsystem, VoltageMonitorSubsystem voltageMonitor
    ) {
        this.intakeDeploy = intakeDeploy;
        this.hoodSubsystem = hoodSubsystem;
        this.voltageMonitor = voltageMonitor;
        if (electricalContract.isLightsReady()) {
            this.lights = lightsFactory.create(
                    electricalContract.getLightControllerInfo()
            );
        } else {
            this.lights = null;
        }
    }

    @Override
    public void periodic() {
        super.periodic();
        if (lights == null) {
            return;
        }

        if (intakeDeploy.isCalibrated && DriverStation.isAutonomous() && voltageMonitor.isAtUnhealthyVoltage()) {
            lights.larson(0, Hertz.of(25), Color.kDodgerBlue, LarsonBounceValue.Back);
        } else if (intakeDeploy.isCalibrated && DriverStation.isTeleop() && voltageMonitor.isAtUnhealthyVoltage()) {
            lights.larson(0, Hertz.of(25), Color.kGreen, LarsonBounceValue.Back);
        } else {
            lights.larson(0, Hertz.of(25), Color.kFirstRed, LarsonBounceValue.Back);
        }

        if (hoodSubsystem.getCurrentValue() >= 0.02) {
            lights.larson(1, Hertz.of(25), Color.kDarkRed, LarsonBounceValue.Front);
        } else {
            lights.larson(1, Hertz.of(25), Color.kLightGreen, LarsonBounceValue.Front);
        }

        var alliance = DriverStation.getAlliance().orElse(Alliance.Blue);

        if (alliance == Alliance.Blue){
            lights.larson(2, Hertz.of(25), Color.kBlue, LarsonBounceValue.Front);
        } else if (alliance == Alliance.Red) {
            lights.larson(2, Hertz.of(25), Color.kRed, LarsonBounceValue.Front);
        } else {
            lights.larson(2, Hertz.of(25), Color.kWhite, LarsonBounceValue.Front);
        }
    }
}
