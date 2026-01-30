package competition.subsystems.lights;


import com.ctre.phoenix6.signals.AnimationDirectionValue;
import com.ctre.phoenix6.signals.LarsonBounceValue;
import competition.electrical_contract.ElectricalContract;
import competition.subsystems.shooter_feeder.ShooterFeederSubsystem;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj2.command.Subsystem;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANLightController;
import xbot.common.controls.actuators.XCANMotorControllerPIDProperties;
import xbot.common.injection.electrical_contract.CANBusId;
import xbot.common.injection.electrical_contract.CANLightControllerInfo;
import xbot.common.injection.electrical_contract.CANLightControllerOutputConfig;
import xbot.common.injection.electrical_contract.LEDStripType;
import xbot.common.injection.electrical_contract.LightControllerType;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.Hertz;


@Singleton
public class LightsSubsystem extends BaseSubsystem {
    public final XCANLightController lights;

    @Inject
    public LightsSubsystem(XCANLightController.XCANLightControllerFactory lightsFactory,
                           ElectricalContract electricalContract, PropertyFactory propertyFactory) {

        propertyFactory.setPrefix(this);
        if (electricalContract.isLightsReady()) {
            this.lights = lightsFactory.create(
                    electricalContract.getLightControlerInfo());
        } else {
            this.lights = null;
        }
    }



    @Override
    public void periodic() {
        super.periodic();
        if (lights != null) {
            lights.larson(0, Hertz.of(25), Color.kDodgerBlue, LarsonBounceValue.Back);
        }
}
