package competition.auto_programs;

import competition.command_groups.AutoShootFuelAtStartCommandGroup;
import competition.subsystems.hood.commands.HoodSetCommand;
import competition.subsystems.shooter.ShooterSubsystem;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.RPM;

public class JustFireStartingFuelAuto extends BaseAutonomousSequentialCommandGroup {

    @Inject
    public JustFireStartingFuelAuto(AutonomousCommandSelector autoSelector,
                                    AutoShootFuelAtStartCommandGroup autoShootFuelAtStartCommandGroup,
                                    ShooterSubsystem shooter,
                                    HoodSetCommand hoodSetCommand) {
        super(autoSelector);


        shooter.setTargetValue(RPM.of(3800));
        hoodSetCommand.setTargetRatio(.6);
        addCommands(autoShootFuelAtStartCommandGroup);

    }
}
