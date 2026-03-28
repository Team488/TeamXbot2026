package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import edu.wpi.first.wpilibj.Timer;
import xbot.common.command.BaseCommand;
import xbot.common.math.XYPair;

import javax.inject.Inject;

public class DriveForwardToClimbCommand extends BaseCommand {
    private final DriveSubsystem drive;
    private final Timer timer = new Timer();

    private double durationSeconds;
    private double forwardPower;

    @Inject
    public DriveForwardToClimbCommand(DriveSubsystem drive) {
        this.drive = drive;
        this.addRequirements(drive);
    }

    public DriveForwardToClimbCommand setDuration(double seconds) {
        this.durationSeconds = seconds;
        return this;
    }

    public DriveForwardToClimbCommand setPower(double power) {
        this.forwardPower = power;
        return this;
    }

    @Override
    public void initialize() {
        super.initialize();
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
        super.end(interrupted);
        drive.stop();
    }
}