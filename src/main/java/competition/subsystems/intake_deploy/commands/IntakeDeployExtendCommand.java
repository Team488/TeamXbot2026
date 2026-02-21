package competition.subsystems.intake_deploy.commands;

import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import xbot.common.command.BaseCommand;
import xbot.common.command.BaseSetpointCommand;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.Degree;

public class IntakeDeployExtendCommand extends BaseSetpointCommand {
    final IntakeDeploySubsystem intakeDeploy;

    @Inject
    public IntakeDeployExtendCommand(IntakeDeploySubsystem intakeDeploy) {
        super(intakeDeploy);
        this.intakeDeploy = intakeDeploy;
    }

    @Override
    public void initialize() {
        intakeDeploy.setTargetValue(Degree.of(intakeDeploy.extendedPositionInDegree.get()));
        log.info("Initialized IntakeDeployExtend");
    }

    @Override
    public boolean isFinished() {
        return false;
    }

}
