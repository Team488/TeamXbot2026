package competition.subsystems.intake_deploy;

import competition.subsystems.shooter.ShooterSubsystem;
import edu.wpi.first.wpilibj2.command.Command;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.Degree;

public class IntakeDeployRetractCommand extends BaseCommand {

    IntakeDeploySubsystem intakeDeploy;

    @Inject
    public IntakeDeployRetractCommand(IntakeDeploySubsystem intakeDeploySubsystem) {
        intakeDeploy = intakeDeploySubsystem;
        this.addRequirements(intakeDeploy);
    }

    @Override
    public void initialize() {
        Degree.of(90);
        intakeDeploy.setTargetValue(Degree.of(90));
    }
}
