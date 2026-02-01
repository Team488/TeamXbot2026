package competition.subsystems.intake_deploy;

import xbot.common.command.BaseCommand;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.Degree;

public class IntakeDeployExtendCommand extends BaseCommand {

    IntakeDeploySubsystem intakeDeploy;

    @Inject
    public IntakeDeployExtendCommand(IntakeDeploySubsystem intakeDeploySubsystem) {
        intakeDeploy = intakeDeploySubsystem;
        this.addRequirements(intakeDeploy);
    }

    @Override
    public void initialize() {
        Degree.of(90);
        intakeDeploy.setTargetValue(Degree.of(90));
    }
}
