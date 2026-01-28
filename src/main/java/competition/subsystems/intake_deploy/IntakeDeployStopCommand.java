package competition.subsystems.intake_deploy;

import competition.subsystems.shooter.ShooterSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;


public class IntakeDeployStopCommand extends BaseCommand {
    IntakeDeploySubsystem intakeDeploy;

    @Inject
    public IntakeDeployStopCommand(IntakeDeploySubsystem intakeDeploySubsystem) {
            intakeDeploy = intakeDeploySubsystem;
            this.addRequirements(intakeDeploy);
    }

    @Override
    public void initialize() {
        intakeDeploy.stop();
    }
}
