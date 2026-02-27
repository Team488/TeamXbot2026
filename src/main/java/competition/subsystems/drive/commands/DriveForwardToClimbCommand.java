package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import edu.wpi.first.wpilibj.Timer;
import xbot.common.command.BaseCommand;
import xbot.common.math.XYPair;

import javax.inject.Inject;

public class DriveForwardToClimbCommand extends BaseCommand {
    private final DriveSubsystem drive;
    private final Timer timer = new Timer();

    private final double durationSeconds;
    private final double forwardPower;

    @Inject
    public DriveForwardToClimbCommand(DriveSubsystem drive) {
        this(drive, 2.0, 0.3);
    }

    public DriveForwardToClimbCommand(DriveSubsystem drive, double durationSeconds, double forwardPower) {
        this.drive = drive;
        this.durationSeconds = durationSeconds;
        this.forwardPower = forwardPower;
        this.addRequirements(drive);
    }

    @Override
    public void initialize() {
        timer.reset();
        timer.start();
    }

    @Override
    public void execute() {
        drive.fieldOrientedDrive(
                new XYPair(forwardPower, 0), // forward only
                0, // no rotation
                0,
                true
        );
    }

    @Override
    public boolean isFinished() {
        return timer.get() >= durationSeconds;
    }

    @Override
    public void end(boolean interrupted) {
        drive.stop();
    }
}