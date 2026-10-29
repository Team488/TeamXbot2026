package competition.command_groups;

import competition.subsystems.drive.commands.WaitForLookAtPointCommand;
import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.shooter.ShooterSubsystem;


import javax.inject.Inject;

public class WaitForRotationAndHoodAndShooterToBeAtGoalCommandGroup extends WaitForHoodAndShooterToBeAtGoalCommandGroup {
    @Inject
    public WaitForRotationAndHoodAndShooterToBeAtGoalCommandGroup(ShooterSubsystem shooterSubsystem,
                                                       HoodSubsystem hoodSubsystem,
                                                       WaitForLookAtPointCommand waitForLookAtPointCommand) {
        super(shooterSubsystem, hoodSubsystem);

        this.addCommands(waitForLookAtPointCommand);
    }
}
