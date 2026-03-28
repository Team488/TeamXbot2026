package competition.subsystems.lights;

import com.ctre.phoenix6.signals.LarsonBounceValue;
import competition.electrical_contract.ElectricalContract;
import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import competition.subsystems.shooter.ShooterSubsystem;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.util.Color;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANLightController;
import competition.subsystems.voltage_alert.VoltageMonitorSubsystem;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.Hertz;

@Singleton
public class LightsSubsystem extends BaseSubsystem {
    public final XCANLightController lights;
    public IntakeDeploySubsystem intakeDeploy;
    public HoodSubsystem hoodSubsystem;
    public VoltageMonitorSubsystem voltageMonitor;
    public ShooterSubsystem shooterSubsystem;

    @Inject
    public LightsSubsystem(XCANLightController.XCANLightControllerFactory lightsFactory,
                           ElectricalContract electricalContract,
                           IntakeDeploySubsystem intakeDeploy,
                           HoodSubsystem hoodSubsystem, VoltageMonitorSubsystem voltageMonitor,
                           ShooterSubsystem shooterSubsystem
    ) {
        this.intakeDeploy = intakeDeploy;
        this.hoodSubsystem = hoodSubsystem;
        this.voltageMonitor = voltageMonitor;
        this.shooterSubsystem = shooterSubsystem;

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

        if (shooterSubsystem.isReadyToFire()) { //red
            lights.fire(2, Hertz.of(25), 25, 25, 25);
        } else if (shooterSubsystem.isReadyToFire()){
            return;
        }
        // Slot 2 should be hood left
        //Slot 3 should be hood right

        if (hoodSubsystem.getCurrentValue() >= 0.02) {
            lights.larson(1, Hertz.of(25), Color.kDarkRed, LarsonBounceValue.Front);
        } else {
            lights.larson(1, Hertz.of(25), Color.kLightGreen, LarsonBounceValue.Front);
        }
    }
}
