package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import xbot.common.command.BaseCommand;
import xbot.common.controls.sensors.XTimer;
import xbot.common.math.XYPair;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

public class DriveToOutpostCommand extends BaseCommand {
    final DriveSubsystem drive;

    DoubleProperty moveRobotX;
    final DoubleProperty timeAmountToMove;

    double startTime;

    @Inject
    public DriveToOutpostCommand(DriveSubsystem drive, PropertyFactory pf) {
        pf.setPrefix(this);
        this.drive = drive;
        this.addRequirements(drive);
        this.moveRobotX = pf.createPersistentProperty("Move Robot X", 2);
        this.timeAmountToMove = pf.createPersistentProperty("Time Amount To Move", 2);
    }

    @Override
    public void initialize() {
        startTime = XTimer.getMatchTime();
    }
    @Override
    public void execute() {
        drive.drive(new XYPair(moveRobotX.get(), 0),0);
    }

    @Override
    public boolean isFinished() {
        return XTimer.getFPGATimestamp() - startTime > timeAmountToMove.get();
    }
}
