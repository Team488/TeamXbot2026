package competition.subsystems.intake_deploy.commands;

import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class IntakeDeployRetractCommand extends BaseCommand {
    final IntakeDeploySubsystem intakeDeploy;

    @Inject
    public IntakeDeployRetractCommand(IntakeDeploySubsystem intakeDeploy) {
        this.intakeDeploy = intakeDeploy;
        this.addRequirements(intakeDeploy);
    }

    @Override
    public void initialize() {
        intakeDeploy.retract();
    }
}
