package competition.subsystems.lights;

import com.ctre.phoenix6.signals.LarsonBounceValue;
import competition.electrical_contract.ElectricalContract;
import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import edu.wpi.first.wpilibj.util.Color;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANLightController;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.Hertz;

@Singleton
public class LightsSubsystem extends BaseSubsystem {
    public final XCANLightController lights;
    public IntakeDeploySubsystem intakeDeploy;

    @Inject
    public LightsSubsystem(XCANLightController.XCANLightControllerFactory lightsFactory,
                           ElectricalContract electricalContract,
                           IntakeDeploySubsystem intakeDeploy) {
        this.intakeDeploy = intakeDeploy;
        if (electricalContract.isLightsReady()) {
            this.lights = lightsFactory.create(
                    electricalContract.getLightControllerInfo());
        } else {
            this.lights = null;
        }
    }

    @Override
    public void periodic() {
        super.periodic();
        if (lights != null) {
            if (intakeDeploy.isCalibrated) {
                lights.larson(0, Hertz.of(25), Color.kHotPink, LarsonBounceValue.Back);
            } else {
                lights.larson(0, Hertz.of(25), Color.kDodgerBlue, LarsonBounceValue.Back);
            }
        }
    }
}
