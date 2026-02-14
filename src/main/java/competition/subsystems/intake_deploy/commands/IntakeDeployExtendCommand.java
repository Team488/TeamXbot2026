package competition.subsystems.intake_deploy.commands;

import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.Degree;

public class IntakeDeployExtendCommand extends BaseCommand {
    final IntakeDeploySubsystem intakeDeploy;

    @Inject
    public IntakeDeployExtendCommand(IntakeDeploySubsystem intakeDeploy) {
        this.intakeDeploy = intakeDeploy;
    }

    @Override
    public void initialize() {
        intakeDeploy.setTargetValue(Degree.of(intakeDeploy.extendedPositionInDegree.get()));
    }

    @Override
    public boolean isFinished() {
        return true;
    }

    @Override
    public void execute() {
        log.info("Intake is extending!");
    }
}
