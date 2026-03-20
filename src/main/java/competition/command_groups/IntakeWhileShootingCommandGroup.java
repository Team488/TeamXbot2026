package competition.command_groups;


import competition.subsystems.intake_deploy.commands.IntakeDeploySlowClosing;
import xbot.common.command.BaseSequentialCommandGroup;

public class IntakeWhileShootingCommandGroup extends BaseSequentialCommandGroup {

    public IntakeWhileShootingCommandGroup(FireWhenShooterAndHoodReady fireWhenShooterAndHoodReady,
                                                  IntakeDeploySlowClosing intakeDeploySlowClosing) {
        this.addCommands(
                (fireWhenShooterAndHoodReady)
                        .andThen(intakeDeploySlowClosing)
        );
    }
}
