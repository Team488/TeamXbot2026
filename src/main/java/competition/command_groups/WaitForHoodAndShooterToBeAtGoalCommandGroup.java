package competition.command_groups;

import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.shooter.ShooterSubsystem;
import xbot.common.command.BaseParallelCommandGroup;

import javax.inject.Inject;

public class WaitForHoodAndShooterToBeAtGoalCommandGroup extends BaseParallelCommandGroup {

    @Inject
    public WaitForHoodAndShooterToBeAtGoalCommandGroup(ShooterSubsystem shooterSubsystem,
                                                       HoodSubsystem hoodSubsystem) {
        var waitForShooterCommand = shooterSubsystem.getWaitForAtGoalCommand();
        var waitForHoodCommand = hoodSubsystem.getWaitForAtGoalCommand();

        this.addCommands(waitForShooterCommand, waitForHoodCommand);
    }
}