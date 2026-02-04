package competition.subsystems.voltage_alert;


import competition.subsystems.climber.ClimberSubsystem;
import edu.wpi.first.wpilibj.Alert;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.sensors.XPowerDistributionPanel;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class VoltageMonitorSubsystem extends BaseSubsystem {

    final XPowerDistributionPanel powerDistribution;

    private final Alert voltageAlert = new Alert("The Voltage is Low!", Alert.AlertType.kWarning);

    @Inject
    public VoltageMonitorSubsystem(XPowerDistributionPanel powerDistribution) {

        this.powerDistribution = powerDistribution;
    }

    @Override
    public void periodic(){
        this.powerDistribution.getVoltage();
        if (this.powerDistribution.getVoltage() < 8) { voltageAlert.set(true);
        }
    }
}