package competition.auto_programs.vision;

import competition.auto_programs.BaseAutonomousSequentialCommandGroup;
import competition.command_groups.DriveToOutpostAndDeployHopperCommandGroupFactory;
import competition.command_groups.DriveWithinAllianceAndShootCommandGroupFactory;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

import javax.inject.Inject;
import javax.inject.Provider;

public class CollectFromOutpostAndShootCommandGroup extends BaseAutonomousSequentialCommandGroup {

    @Inject
    public CollectFromOutpostAndShootCommandGroup(AutonomousCommandSelector autoSelector,
            Provider<DriveToOutpostAndDeployHopperCommandGroupFactory> driveToOutpostAndDeployHopperCommandGroupFactory,
            Provider<DriveWithinAllianceAndShootCommandGroupFactory> driveWithinAllianceAndShootCommandGroupFactory) {
        super(autoSelector);

        getAutoStatusChangeCommand("Starting CollectFromOutpostAndShootCommandGroup");
        var driveAndDeployCommand = driveToOutpostAndDeployHopperCommandGroupFactory.get().create()
                .alongWith(getAutoStatusChangeCommand("Driving to outpost while deploying"));

        this.addCommands(driveAndDeployCommand);


        var driveToShoot = driveWithinAllianceAndShootCommandGroupFactory.get().create()
                .alongWith(getAutoStatusChangeCommand("Heading to shoot"));

        this.addCommands(driveToShoot);
    }
}
