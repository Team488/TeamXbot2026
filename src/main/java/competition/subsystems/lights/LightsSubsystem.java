package competition.subsystems.lights;

import com.ctre.phoenix6.signals.LarsonBounceValue;
import competition.electrical_contract.ElectricalContract;
import competition.subsystems.hood.HoodSubsystem;
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
    public HoodSubsystem hoodSubsystem;

    @Inject
    public LightsSubsystem(XCANLightController.XCANLightControllerFactory lightsFactory,
                           ElectricalContract electricalContract,
                           IntakeDeploySubsystem intakeDeploy,
                           HoodSubsystem hoodSubsystem
    ) {
        this.intakeDeploy = intakeDeploy;
        this.hoodSubsystem = hoodSubsystem;
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

        if (hoodSubsystem.getCurrentValue() >= 0.02) {
            lights.larson(8, Hertz.of(25), Color.kDarkRed, LarsonBounceValue.Front);
        } else {
            lights.larson(8, Hertz.of(25), Color.kLightGreen, LarsonBounceValue.Front);
        }

//        if (intakeDeploy.isCalibrated) {
//            lights.larson(0, Hertz.of(25), Color.kHotPink, LarsonBounceValue.Back);
//        } else {
//            lights.larson(0, Hertz.of(25), Color.kDodgerBlue, LarsonBounceValue.Back);
//        }
    }
}
