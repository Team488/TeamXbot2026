package competition.auto_programs.ppl;

import com.pathplanner.lib.auto.AutoBuilder;
import competition.auto_programs.BaseAutonomousSequentialCommandGroup;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

import javax.inject.Inject;

public class RightBumpAutoCommand extends BaseAutonomousSequentialCommandGroup {
    @Inject
    public RightBumpAutoCommand(
            AutonomousCommandSelector autoSelector,
            PropertyFactory pf
    ) {
        super(autoSelector);
        pf.setPrefix(this.getName());

        this.addCommands(AutoBuilder.buildAuto("NormalBumpAutoRight"));
    }
}
