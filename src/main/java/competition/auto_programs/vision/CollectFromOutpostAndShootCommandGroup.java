package competition.auto_programs.vision;

import competition.auto_programs.BaseAutonomousSequentialCommandGroup;
import competition.command_groups.DriveToOutpostAndDeployHopperCommandGroupFactory;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

import javax.inject.Inject;
import javax.inject.Provider;

public class CollectFromOutpostAndShootCommandGroup extends BaseAutonomousSequentialCommandGroup {

    @Inject
    public CollectFromOutpostAndShootCommandGroup(AutonomousCommandSelector autoSelector,
            Provider<DriveToOutpostAndDeployHopperCommandGroupFactory> driveToOutpostAndDeployHopperCommandGroupFactory
            /*Provider<DriveFromNeutralZoneToAllianceAndShootCommandGroupFactory> driveFromNeutralZoneToAllianceAndShootCommandGroupProvider*/) {
        super(autoSelector);

        getAutoStatusChangeCommand("Starting CollectFromOutpostAndShootCommandGroup");
        var driveAndDeployCommand = driveToOutpostAndDeployHopperCommandGroupFactory.get().create()
                .alongWith(getAutoStatusChangeCommand("Driving to outpost while deploying"));

        this.addCommands(driveAndDeployCommand);

        // var driveToAllianceZone = driveFromNeutralZoneToAllianceAndShootCommandGroupProvider.get().create()
        //         .alongWith(getAutoStatusChangeCommand("Driving to alliance and shoot"));

        // this.addCommands(driveToAllianceZone);
    }
}
