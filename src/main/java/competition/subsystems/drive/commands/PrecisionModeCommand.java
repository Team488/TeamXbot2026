package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class PrecisionModeCommand extends BaseCommand {
    
    final DriveSubsystem drive;

    @Inject
    public PrecisionModeCommand(DriveSubsystem drive) {
        this.drive = drive;
    }
    
    @Override
    public void initialize() {
        super.initialize();
        drive.setUnlockFullDrivePower(false);
        drive.setPrecisionTranslationActive(true);
        drive.setPrecisionRotationActive(true);
    }

    @Override
    public void end(boolean isInterrupted) {
        super.end(isInterrupted);
        drive.setUnlockFullDrivePower(true);
        drive.setPrecisionTranslationActive(false);
        drive.setPrecisionRotationActive(false);
    }
}