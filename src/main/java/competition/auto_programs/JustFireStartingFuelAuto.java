package competition.auto_programs;

import competition.command_groups.AutoShootFuelAtStartCommandGroup;
import competition.subsystems.hood.commands.HoodSetCommand;
import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.shooter.commands.ShooterOutputCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.RPM;

public class JustFireStartingFuelAuto extends BaseAutonomousSequentialCommandGroup {

    @Inject
    public JustFireStartingFuelAuto(AutonomousCommandSelector autoSelector,
                                    AutoShootFuelAtStartCommandGroup autoShootFuelAtStartCommandGroup,
                                    ShooterOutputCommand shooterOutputCommand,
                                    HoodSetCommand hoodSetCommand) {
        super(autoSelector);


        shooterOutputCommand.setTargetVelocity(RPM.of(3800));
        hoodSetCommand.setTargetRatio(.6);
        addCommands(
                new ParallelCommandGroup(
                shooterOutputCommand,
                hoodSetCommand
                ),
                autoShootFuelAtStartCommandGroup);

    }
}
