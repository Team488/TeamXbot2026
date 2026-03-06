package competition.command_groups;

import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.AngularVelocity;
import xbot.common.command.BaseSequentialCommandGroup;
import xbot.common.properties.AngularVelocityProperty;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.RPM;

public class AutoShootFuelAtStartCommandGroup extends BaseSequentialCommandGroup {

    final DoubleProperty fireTimeoutSec;
    final DoubleProperty autoStartAngle;
    final AngularVelocityProperty autoStartRPM;

    @Inject
    public AutoShootFuelAtStartCommandGroup(PrepareToShootCommandGroup prepare, FireWhenShooterReadyCommandGroup fire,
                                            PropertyFactory pf, AngularVelocityProperty avp) {
        fireTimeoutSec = pf.createPersistentProperty("Fire Timeout", 4.0);
        autoStartAngle = pf.createPersistentProperty("Hood Angle", 0.5);
        autoStartRPM =

        addCommands(prepare,
                    fire.withTimeout(fireTimeoutSec.get())
        );
    }
}
