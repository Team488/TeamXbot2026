package competition.subsystems.intake_deploy.commands;

import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import xbot.common.command.BaseCommand;
import javax.inject.Inject;

/**
 * This is supposed to calibrate the intake when it has already deployed (when it is down)
 */

public class CalibrateOffsetDown extends BaseCommand {

    final IntakeDeploySubsystem intakeDeploySubsystem;

    @Inject
    public CalibrateOffsetDown(IntakeDeploySubsystem intakeDeploySubsystem) {
        this.intakeDeploySubsystem = intakeDeploySubsystem;
    }

    @Override
    public void initialize() {
        intakeDeploySubsystem.calibrateOffsetDown();
    }

}
