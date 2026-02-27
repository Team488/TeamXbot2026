package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import xbot.common.command.BaseCommand;
import xbot.common.math.XYPair;

import javax.inject.Inject;

public class DriveForwardForTime extends BaseCommand {

    DriveSubsystem drive;

    @Inject
    public DriveForwardForTime (DriveSubsystem driveSubsystem) {
        this.drive = driveSubsystem;
    }

    @Override
    public void initialize() {
        drive.move(new XYPair(1.0,0),0);
    }
}
