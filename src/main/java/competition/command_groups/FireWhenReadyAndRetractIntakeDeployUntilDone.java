package competition.command_groups;

import competition.subsystems.intake_deploy.commands.IntakeDeployRetractCommand;
import xbot.common.command.BaseParallelCommandGroup;

import javax.inject.Inject;


public class FireWhenReadyAndRetractIntakeDeployUntilDone extends BaseParallelCommandGroup {

    @Inject
    public FireWhenReadyAndRetractIntakeDeployUntilDone(FireWhenShooterAndHoodReadyUntilDone fireWhenShooterAndHoodReady,
                                                        IntakeDeployRetractCommand intakeDeployRetractCommand) {
        this.addCommands(fireWhenShooterAndHoodReady, intakeDeployRetractCommand);
    }
}
