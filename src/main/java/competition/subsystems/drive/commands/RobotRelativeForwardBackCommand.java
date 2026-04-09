package competition.subsystems.drive.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.drive.DriveSubsystem;
import xbot.common.command.BaseCommand;
import xbot.common.math.MathUtils;
import xbot.common.math.XYPair;

import javax.inject.Inject;

public class RobotRelativeForwardBackCommand extends BaseCommand {

    private final DriveSubsystem drive;
    private final OperatorInterface oi;

    @Inject
    public RobotRelativeForwardBackCommand(DriveSubsystem drive, OperatorInterface oi) {
        this.drive = drive;
        this.oi = oi;
        addRequirements(drive);
    }

    @Override
    public void execute() {
        double y = MathUtils.deadband(oi.driverGamepad.getLeftVector().getY(), 0.15) * 0.5;
        drive.move(new XYPair(y, 0), 0);
    }
}
