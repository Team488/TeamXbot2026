package competition.command_groups;

import xbot.common.command.BaseSequentialCommandGroup;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

public class AutoShootFuelAtStartCommandGroup extends BaseSequentialCommandGroup {

    final DoubleProperty fireTimeoutSec;

    @Inject
    public AutoShootFuelAtStartCommandGroup(PrepareToShootCommandGroup prepare, FireWhenShooterReadyCommandGroup fire,
                                            PropertyFactory pf) {
        fireTimeoutSec = pf.createPersistentProperty("Fire Timeout", 1.0);

        addCommands(prepare,
                    fire.withTimeout(fireTimeoutSec.get())
        );
    }
}
