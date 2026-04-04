package competition.auto_programs;

import com.pathplanner.lib.auto.AutoBuilder;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

import javax.inject.Inject;

public class RiskyBumpAutoCommand extends BaseAutonomousSequentialCommandGroup {
    @Inject
    public RiskyBumpAutoCommand(
            AutonomousCommandSelector autoSelector,
            PropertyFactory pf
    ) {
        super(autoSelector);
        pf.setPrefix(this.getName());

        this.addCommands(AutoBuilder.buildAuto("RiskyBumpAuto"));
    }
}
