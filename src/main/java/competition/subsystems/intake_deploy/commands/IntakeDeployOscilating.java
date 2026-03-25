package competition.subsystems.intake_deploy.commands;

import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import xbot.common.command.BaseSetpointCommand;

import javax.inject.Inject;

public class IntakeDeployOscilating extends BaseSetpointCommand {
    public final IntakeDeploySubsystem intake;
    public final IntakeDeployRetractCommand retractCommand;
    public final IntakeDeployExtendCommand extendCommand;

    @Inject
    public IntakeDeployOscilating(IntakeDeploySubsystem intakeDeploySubsystem, IntakeDeployExtendCommand
            intakeDeployExtendCommand, IntakeDeployRetractCommand intakeDeployRetractCommand, IntakeDeploySubsystem intake) {
        super(intakeDeploySubsystem);
        this.extendCommand = intakeDeployExtendCommand;
        this.retractCommand = intakeDeployRetractCommand;
        this.intake = intake;
    }
    @Override
    public void execute() {
        intake.
    }
}
