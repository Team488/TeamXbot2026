package competition.auto.pathplanner;

import com.pathplanner.lib.auto.AutoBuilder;
import competition.auto.BaseAutonomousSequentialCommandGroup;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

import javax.inject.Inject;

public class TwoLoopRightBumpAuto extends BaseAutonomousSequentialCommandGroup {
    @Inject
    public TwoLoopRightBumpAuto(
            AutonomousCommandSelector autoSelector,
            PropertyFactory pf
    ) {
        super(autoSelector);
        pf.setPrefix(this.getName());

        this.addCommands(AutoBuilder.buildAuto("TwoLoopRightBumpAuto"));
    }
}
