package competition.general_commands;

import edu.wpi.first.wpilibj.Timer;
import xbot.common.command.BaseCommand;

import java.util.function.Supplier;

public class WaitForDurationCommand extends BaseCommand {

    private final Supplier<Double> durationSeconds;
    private final Timer timer;

    public WaitForDurationCommand(Supplier<Double> durationSeconds) {
        this.durationSeconds = durationSeconds;
        this.timer = new Timer();
    }

    @Override
    public void initialize() {
        super.initialize();
        timer.reset();
        timer.start();
    }

    @Override
    public boolean isFinished() {
        return timer.hasElapsed(durationSeconds.get());
    }

    @Override
    public void end(boolean interrupted) {
        timer.stop();
    }
}
