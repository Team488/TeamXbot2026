package competition.subsystems.intake_deploy.commands;

import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import xbot.common.command.BaseCommand;
import javax.inject.Inject;

/**
 * This is supposed to calibrate the intake when it has not deployed (when it is up)
 */

public class CalibrateOffsetUp extends BaseCommand{

    final IntakeDeploySubsystem intakeDeploySubsystem;

    @Inject
    public CalibrateOffsetUp(IntakeDeploySubsystem intakeDeploySubsystem) {
        this.intakeDeploySubsystem = intakeDeploySubsystem;
    }

    @Override
    public void initialize() {
        intakeDeploySubsystem.calibrateOffsetUp();
        log.info("Initialized CalibrateOffsetUp");
    }

}
