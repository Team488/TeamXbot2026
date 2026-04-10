package competition.subsystems.lights;

import com.ctre.phoenix6.signals.LarsonBounceValue;
import competition.electrical_contract.ElectricalContract;
import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import competition.subsystems.vision.AprilTagVisionSubsystemExtended;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.util.Color;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANLightController;
import competition.subsystems.voltage_alert.VoltageMonitorSubsystem;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import xbot.common.logging.RobotAssertionManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.Hertz;

@Singleton
public class LightsSubsystem extends BaseSubsystem {
    public final XCANLightController lights;
    public IntakeDeploySubsystem intakeDeploy;
    public HoodSubsystem hoodSubsystem;
    public VoltageMonitorSubsystem voltageMonitor;
    public RobotAssertionManager assertionManager;
    public final AprilTagVisionSubsystemExtended vision;

    @Inject
    public LightsSubsystem(XCANLightController.XCANLightControllerFactory lightsFactory,
                           ElectricalContract electricalContract,
                           IntakeDeploySubsystem intakeDeploy,
                           HoodSubsystem hoodSubsystem,
                           VoltageMonitorSubsystem voltageMonitor,
                           RobotAssertionManager assertionManager,
                           AprilTagVisionSubsystemExtended vision
    ) {
        this.intakeDeploy = intakeDeploy;
        this.hoodSubsystem = hoodSubsystem;
        this.voltageMonitor = voltageMonitor;
        this.assertionManager = assertionManager;
        this.vision = vision;
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

        if (DriverStation.isDisabled() && !vision.areAllCamerasConnected()) {
            lights.strobe(0, Hertz.of(1), Color.kRed);
        } else if (intakeDeploy.isCalibrated && DriverStation.isAutonomous() && voltageMonitor.isAtUnhealthyVoltage()) {
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

        switch (alliance) {
            case Blue -> lights.larson(2, Hertz.of(25), Color.kBlue, LarsonBounceValue.Front);
            case Red -> lights.larson(2, Hertz.of(25), Color.kRed, LarsonBounceValue.Front);
            default -> assertionManager.throwException("No Alliance Selected!", new Exception());
        }
    }
}
