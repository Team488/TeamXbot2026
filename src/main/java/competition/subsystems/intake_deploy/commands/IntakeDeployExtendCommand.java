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
        this.addRequirements(intakeDeploy);
    }

    @Override
    public void initialize() {
        Degree.of(90);
        intakeDeploy.setTargetValue(Degree.of(90));
    }
}
