package competition.operator_interface;

import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.wpilibj.DriverStation;
import xbot.common.command.BaseCommand;
import xbot.common.properties.DistanceProperty;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.feedback.XRumbleManager;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.Meters;

public class DriverRumbleCommand extends BaseCommand {
    private final XRumbleManager rumbleManager;
    private final PoseSubsystem pose;
    private final HoodSubsystem hood;
    private final DoubleProperty hoodAlertThreshold;
    private final DistanceProperty trenchAlertDistance;

    @Inject
    public DriverRumbleCommand(PropertyFactory pf, OperatorInterface oi, PoseSubsystem pose, HoodSubsystem hood) {
        this.rumbleManager = oi.driverGamepad.getRumbleManager();
        this.pose = pose;
        this.hood = hood;

        pf.setPrefix(this);
        this.hoodAlertThreshold = pf.createPersistentProperty("Hood Alert Threshold", 0.2);
        this.trenchAlertDistance = pf.createPersistentProperty("Trench Alert Distance", Meters.of(1.5));
    }

    @Override
    public void execute() {
        super.execute();
        var distanceToTrench = pose.distanceToNearestTrench();
        aKitLog.record("DistanceToTrench", distanceToTrench);
        if (DriverStation.isTeleop()
                && hood.getCurrentValue() > hoodAlertThreshold.get()
                && distanceToTrench.lt(trenchAlertDistance.get())) {
            rumbleManager.rumbleGamepad(0.2, 0.5);
            logIsRumbling(true);
        } else {
            rumbleManager.stopGamepadRumble();
            logIsRumbling(false);
        }
    }

    @Override
    public void end(boolean isInterrupted) {
        super.end(isInterrupted);
        rumbleManager.stopGamepadRumble();
        logIsRumbling(false);
    }

    private void logIsRumbling(boolean isRumbling) {
        aKitLog.record("IsRumbling", isRumbling);
    }

    @Override
    public boolean runsWhenDisabled() {
        return true;
    }
}
