package competition.subsystems.voltage_alert;

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
    public VoltageMonitorSubsystem(XPowerDistributionPanel.XPowerDistributionPanelFactory powerDistributionPanelFactory) {

        powerDistribution = powerDistributionPanelFactory.create();
    }

    @Override
    public void periodic(){
        if (this.powerDistribution.getVoltage() < 8) { voltageAlert.set(true);
        }
    }
}