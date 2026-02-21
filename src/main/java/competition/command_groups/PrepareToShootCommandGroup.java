package competition.command_groups;

import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.shooter.ShooterSubsystem;
import xbot.common.command.BaseParallelCommandGroup;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.RPM;

public class PrepareToShootCommandGroup extends BaseParallelCommandGroup {

    @Inject
    public PrepareToShootCommandGroup(ShooterSubsystem shooter, HoodSubsystem hood) {
        this.addCommands(
                shooter.setShooterTo(RPM.of(0)),
                hood.setHoodTo(0)
        );
    }
}


