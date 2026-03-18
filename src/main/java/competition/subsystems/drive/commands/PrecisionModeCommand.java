package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class PrecisionModeCommand extends BaseCommand {
    
    DriveSubsystem drive;

    @Inject
    public PrecisionModeCommand(DriveSubsystem drive) {
        this.drive = drive;
    }
    
    @Override
    public void initialize() {
            drive.setPrecisionTranslationActive(true);
            drive.setPrecisionRotationActive(true);
        }


    @Override
    public void end(boolean isInterrupted){
        drive.setPrecisionTranslationActive(false);
        drive.setPrecisionRotationActive(false);
    }

}