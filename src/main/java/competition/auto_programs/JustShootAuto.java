package competition.auto_programs;

import competition.command_groups.FireWhenShooterReadyCommandGroup;
import competition.command_groups.PrepareToShootCommandGroup;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

public class JustShootAuto extends BaseAutonomousSequentialCommandGroup {

    public JustShootAuto(AutonomousCommandSelector autoSelector,
                         PrepareToShootCommandGroup prepareToShootCommandGroup,
                         FireWhenShooterReadyCommandGroup fireWhenShooterReadyCommandGroup) {
        super(autoSelector);


    }
}
