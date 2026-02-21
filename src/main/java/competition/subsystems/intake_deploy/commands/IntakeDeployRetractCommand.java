package competition.subsystems.intake_deploy.commands;

import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import xbot.common.command.BaseSetpointCommand;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.Degree;

public class IntakeDeployRetractCommand extends BaseSetpointCommand {
    final IntakeDeploySubsystem intakeDeploy;

    @Inject
    public IntakeDeployRetractCommand(IntakeDeploySubsystem intakeDeploy) {
        super(intakeDeploy);
        this.intakeDeploy = intakeDeploy;
    }

    @Override
    public void initialize() {
        intakeDeploy.setTargetValue(Degree.of(intakeDeploy.retractedPositionInDegree.get()));
        log.info("Initialized IntakeDeployRetract");
    }

    @Override
    public boolean isFinished() {
        return false;
    }

}
