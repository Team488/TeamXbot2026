package competition.command_groups;


import competition.subsystems.intake_deploy.commands.IntakeDeploySlowClosing;
import xbot.common.command.BaseSequentialCommandGroup;

public class IntakeWhileShootingCommandGroup extends BaseSequentialCommandGroup {

    public IntakeWhileShootingCommandGroup(FireWhenReadyShooterCommandGroup fireWhenReadyShooterCommandGroup,
                                                  IntakeDeploySlowClosing intakeDeploySlowClosing) {
        this.addCommands(
                (fireWhenReadyShooterCommandGroup)
                        .andThen(intakeDeploySlowClosing)
        );
    }
}
