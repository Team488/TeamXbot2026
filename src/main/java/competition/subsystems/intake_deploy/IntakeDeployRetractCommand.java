package competition.subsystems.intake_deploy;

import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class IntakeDeployRetractCommand extends BaseCommand {

    IntakeDeploySubsystem intakeDeploy;

    @Inject
    public IntakeDeployRetractCommand(IntakeDeploySubsystem intakeDeploySubsystem) {
        intakeDeploy = intakeDeploySubsystem;
        this.addRequirements(intakeDeploy);
    }

    @Override
    public void initialize() {
        intakeDeploy.retract();
    }
}
