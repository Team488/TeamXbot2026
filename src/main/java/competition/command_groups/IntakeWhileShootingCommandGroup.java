package competition.command_groups;


import competition.subsystems.intake_deploy.commands.IntakeDeployOscillateControlledClosing;
import xbot.common.command.BaseSequentialCommandGroup;

public class IntakeWhileShootingCommandGroup extends BaseSequentialCommandGroup {

    public IntakeWhileShootingCommandGroup(FireWhenReadyShooterCommandGroup fireWhenReadyShooterCommandGroup,
                                                  IntakeDeployOscillateControlledClosing intakeDeployOscillateControlledClosing) {
        this.addCommands(
                (fireWhenReadyShooterCommandGroup)
                        .andThen(intakeDeployOscillateControlledClosing)
        );
    }
}
