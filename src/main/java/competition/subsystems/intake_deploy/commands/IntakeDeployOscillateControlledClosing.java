package competition.subsystems.intake_deploy.commands;

import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import edu.wpi.first.units.measure.Angle;
import xbot.common.command.BaseCommand;
import xbot.common.command.BaseSetpointCommand;

public class IntakeDeployOscillateControlledClosing extends BaseSetpointCommand {

    public IntakeDeploySubsystem intakeDeploySubsystem;
    public IntakeDeployRetractCommand intakeDeployRetractCommand;
    public IntakeDeployExtendCommand intakeDeployExtendCommand;

    public IntakeDeployOscillateControlledClosing(IntakeDeploySubsystem intakeDeploy,
                                                  IntakeDeployExtendCommand extend,
                                                  IntakeDeployRetractCommand retract) {
        super(intakeDeploy);
        this.intakeDeploySubsystem = intakeDeploy;
        this.intakeDeployExtendCommand = extend;
        this.intakeDeployRetractCommand = retract;
    }

    public void execute() {
        if (intakeDeployExtendCommand != null) {
            intakeDeployExtendCommand.repeatedly();
        }
        else {
            intakeDeployRetractCommand.repeatedly();
        }
    }

    public void end(boolean interrupted) {
        intakeDeployExtendCommand.isFinished();
        intakeDeployRetractCommand.isFinished();
    }
}
