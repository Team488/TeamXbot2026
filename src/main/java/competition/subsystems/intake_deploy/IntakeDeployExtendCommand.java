package competition.subsystems.intake_deploy;

import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class IntakeDeployExtendCommand extends BaseCommand {

    IntakeDeploySubsystem intakeDeploy;

    @Inject
    public IntakeDeployExtendCommand(IntakeDeploySubsystem intakeDeploySubsystem) {
        intakeDeploy = intakeDeploySubsystem;
        this.addRequirements(intakeDeploy);
    }

    @Override
    public void initialize() {
        intakeDeploy.extend();
    }
}
