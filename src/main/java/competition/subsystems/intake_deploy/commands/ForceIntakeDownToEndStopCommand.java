package competition.subsystems.intake_deploy.commands;

import xbot.common.command.BaseParallelDeadlineGroup;

import javax.inject.Inject;

/**
 * Move the intake down under non-PID control to force it down until it hits the end-stop.
 */
public class ForceIntakeDownToEndStopCommand extends BaseParallelDeadlineGroup {
    @Inject
    public ForceIntakeDownToEndStopCommand(
            IntakeDeployExtendWithoutPidCommand extendCommand,
            IntakeDeployWaitForEndStopOrCurrentLimitCommand endStopCommand) {
        super(endStopCommand, extendCommand);
    }
}
