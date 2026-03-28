package competition.subsystems.intake_deploy.commands;

import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import xbot.common.command.BaseCommand;

public class IntakeDeployRetryCommand extends BaseCommand {
    IntakeDeploySubsystem intakeDeploySubsystem;

    public IntakeDeployRetryCommand(IntakeDeploySubsystem intakeDeploy) {
        this.intakeDeploySubsystem = intakeDeploy;
        addRequirements(intakeDeploy);
    }

    @Override
    public boolean isFinished() {
        return intakeDeploySubsystem.intakeDeployIsExtended();
    }
}
