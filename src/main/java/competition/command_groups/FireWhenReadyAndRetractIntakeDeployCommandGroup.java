package competition.command_groups;

import competition.subsystems.intake_deploy.commands.IntakeDeployRetractCommand;
import xbot.common.command.BaseParallelCommandGroup;
import javax.inject.Inject;



public class FireWhenReadyAndRetractIntakeDeployCommandGroup extends BaseParallelCommandGroup {

    @Inject
    public FireWhenReadyAndRetractIntakeDeployCommandGroup(FireWhenReadyShooterCommandGroup fireWhenReadyShooterCommandGroup,
                                                           IntakeDeployRetractCommand intakeDeployRetractCommand) {
        this.addCommands(fireWhenReadyShooterCommandGroup, intakeDeployRetractCommand);
    }
}
