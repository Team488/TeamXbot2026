package competition.command_groups;

import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.AngularVelocity;
import xbot.common.command.BaseSequentialCommandGroup;
import xbot.common.properties.AngularVelocityProperty;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.RPM;
import static edu.wpi.first.units.Units.RotationsPerSecond;

public class AutoShootFuelAtStartCommandGroup extends BaseSequentialCommandGroup {

    final DoubleProperty fireTimeoutSec;
    final DoubleProperty autoStartAngle;
    final AngularVelocityProperty autoStartRPM;

    @Inject
    public AutoShootFuelAtStartCommandGroup(PrepareToShootCommandGroup prepare, FireWhenShooterReadyCommandGroup fire,
                                            PropertyFactory pf) {
        fireTimeoutSec = pf.createPersistentProperty("Fire Timeout", 4.0);
        autoStartAngle = pf.createPersistentProperty("Hood Angle", 0.5);
        autoStartRPM = pf.createPersistentProperty("Rotation Per Second", RotationsPerSecond.of(50));

        prepare.setHoodGoal(autoStartAngle.get());
        prepare.setShooterGoal(autoStartRPM.get());

        addCommands(prepare, fire.withTimeout(fireTimeoutSec.get()));
    }
}
