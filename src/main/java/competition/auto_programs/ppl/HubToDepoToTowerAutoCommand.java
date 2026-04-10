package competition.auto_programs.ppl;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.commands.PathPlannerAuto;
import competition.auto_programs.BaseAutonomousSequentialCommandGroup;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

import javax.inject.Inject;

public class HubToDepoToTowerAutoCommand extends BaseAutonomousSequentialCommandGroup {
    @Inject
    public HubToDepoToTowerAutoCommand(
            AutonomousCommandSelector autoSelector,
            PropertyFactory pf
    ) {
        super(autoSelector);
        pf.setPrefix(this.getName());

        this.addCommands(AutoBuilder.buildAuto("HubToDepoToTowerAuto"));
    }
}
