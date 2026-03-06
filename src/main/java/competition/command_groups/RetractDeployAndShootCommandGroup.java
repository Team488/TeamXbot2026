package competition.command_groups;

import competition.subsystems.intake_deploy.commands.IntakeDeployRetractCommand;
import xbot.common.command.BaseParallelCommandGroup;

import javax.inject.Inject;



public class RetractDeployAndShootCommandGroup extends BaseParallelCommandGroup {

    @Inject
    public RetractDeployAndShootCommandGroup(FireWhenShooterReadyCommandGroup fireWhenShooterReadyCommandGroup,
                                             IntakeDeployRetractCommand intakeDeployRetractCommand) {
        this.addCommands(fireWhenShooterReadyCommandGroup, intakeDeployRetractCommand);
    }
}
