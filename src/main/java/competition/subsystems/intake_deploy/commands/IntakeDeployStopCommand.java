package competition.subsystems.intake_deploy.commands;

import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class IntakeDeployStopCommand extends BaseCommand {
    final IntakeDeploySubsystem intakeDeploy;

    @Inject
    public IntakeDeployStopCommand(IntakeDeploySubsystem intakeDeploy) {
        this.intakeDeploy = intakeDeploy;
        this.addRequirements(intakeDeploy);
    }

    @Override
    public void initialize() {
        intakeDeploy.setTargetValue(intakeDeploy.getCurrentValue());
    }
}
