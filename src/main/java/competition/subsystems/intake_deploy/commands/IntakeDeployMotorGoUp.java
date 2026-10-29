package competition.subsystems.intake_deploy.commands;

import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import xbot.common.command.BaseCommand;

public class IntakeDeployMotorGoUp extends BaseCommand {
    final IntakeDeploySubsystem intake;

    public IntakeDeployMotorGoUp(IntakeDeploySubsystem intakeDeploySubsystem) {
        this.intake = intakeDeploySubsystem;
    }

    public void initialize() {
        intake.intakeDeployGoUp();
        log.info("Initialized IntakeDeployMotorGoUp");
    }
}
