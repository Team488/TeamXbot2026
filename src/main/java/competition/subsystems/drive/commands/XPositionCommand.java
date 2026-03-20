package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class XPositionCommand extends BaseCommand {
    final DriveSubsystem drive;

    @Inject
    public XPositionCommand(DriveSubsystem drive) {
        this.drive = drive;
        this.addRequirements(this.drive);
    }

    @Override
    public void initialize() {
        //Turns the wheels into an X shape so it is harder to shove our robot
        drive.setWheelsToXMode();
        log.info("Wheels at the X-Position");
    }
}