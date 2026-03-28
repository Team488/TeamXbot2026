package competition.general_commands;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import edu.wpi.first.wpilibj.Timer;
import xbot.common.command.BaseCommand;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

public class WaitWithPropertyCommand extends BaseCommand {

    private final DoubleProperty waitDurationSeconds;
    private final Timer timer;

    @AssistedInject
    public WaitWithPropertyCommand(@Assisted String name, @Assisted double defaultSeconds, PropertyFactory pf) {
        pf.setPrefix(name);
        this.waitDurationSeconds = pf.createPersistentProperty("WaitSeconds", defaultSeconds);
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
        return timer.hasElapsed(waitDurationSeconds.get());
    }

    @Override
    public void end(boolean interrupted) {
        timer.stop();
    }

    @AssistedFactory
    public interface Factory {
        WaitWithPropertyCommand create(String name);
    }
}
