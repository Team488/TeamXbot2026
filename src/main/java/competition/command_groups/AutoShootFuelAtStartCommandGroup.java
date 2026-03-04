package competition.command_groups;

import xbot.common.command.BaseSequentialCommandGroup;

import javax.inject.Inject;

public class AutoShootFuelAtStartCommandGroup extends BaseSequentialCommandGroup {

    @Inject
    public AutoShootFuelAtStartCommandGroup(PrepareToShootCommandGroup prepare, FireWhenShooterReadyCommandGroup fire) {
        addCommands(prepare, fire);
    }
}
