package competition.subsystems.intake_deploy.commands;

import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import xbot.common.command.BaseCommand;

public class IntakeDeployMotorGoDown extends BaseCommand {

    final IntakeDeploySubsystem intake;

    public IntakeDeployMotorGoDown(IntakeDeploySubsystem intakeDeploySubsystem) {
        this.intake = intakeDeploySubsystem;
    }

    public void initialize() {
        intake.intakeDeployGoDown();
        log.info("Initialized IntakeDeployMotorGoDown");
    }
}
