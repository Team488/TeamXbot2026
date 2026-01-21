package competition.subsystems.lights;


import com.ctre.phoenix6.signals.AnimationDirectionValue;
import com.ctre.phoenix6.signals.LarsonBounceValue;
import competition.subsystems.shooter_feeder.ShooterFeederSubsystem;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj2.command.Subsystem;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANLightController;
import xbot.common.injection.electrical_contract.CANBusId;
import xbot.common.injection.electrical_contract.CANLightControllerInfo;
import xbot.common.injection.electrical_contract.CANLightControllerOutputConfig;
import xbot.common.injection.electrical_contract.LEDStripType;
import xbot.common.injection.electrical_contract.LightControllerType;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.Hertz;


@Singleton
public class LightsSubsystem extends BaseSubsystem {
    public final XCANLightController lights2;

    @Inject
    public LightsSubsystem(XCANLightController.XCANLightControllerFactory lightsFactory) {
        lights2 =  lightsFactory.create(new CANLightControllerInfo("Lights",
                LightControllerType.Candle, CANBusId.DefaultCanivore, 11,
                new CANLightControllerOutputConfig(LEDStripType.GRB, 0.15, new int[] {8})));
        lights2.larson(0, Hertz.of(25), Color.kDarkBlue, LarsonBounceValue.Back);
    }

    @Override
    public void periodic() {
        super.periodic();
    }
}
