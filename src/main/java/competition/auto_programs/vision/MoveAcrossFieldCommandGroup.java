package competition.auto_programs.vision;

import competition.auto_programs.BaseAutonomousSequentialCommandGroup;
import competition.command_groups.DriveFromNeutralZoneToAllianceAndShootCommandGroupFactory;
import competition.command_groups.DriveToNeutralZoneAndDeployIntakeCommandGroupFactory;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

import javax.inject.Inject;
import javax.inject.Provider;

public class MoveAcrossFieldCommandGroup extends BaseAutonomousSequentialCommandGroup {

    @Inject
    public MoveAcrossFieldCommandGroup(AutonomousCommandSelector autoSelector,
            Provider<DriveToNeutralZoneAndDeployIntakeCommandGroupFactory> driveToNeutralZoneAndDeployIntakeCommandProvider,
            Provider<DriveFromNeutralZoneToAllianceAndShootCommandGroupFactory> driveFromNeutralZoneToAllianceAndShootCommandGroupProvider) {
        super(autoSelector);

        getAutoStatusChangeCommand("Starting MoveAcrossFieldCommandGroup");
        var driveToNeutralZone = driveToNeutralZoneAndDeployIntakeCommandProvider.get().create()
                .alongWith(getAutoStatusChangeCommand("Driving to neutral zone and back"));

        this.addCommands(driveToNeutralZone);

        var driveToAllianceZone = driveFromNeutralZoneToAllianceAndShootCommandGroupProvider.get().create()
                .alongWith(getAutoStatusChangeCommand("Driving to alliance and shoot"));

        this.addCommands(driveToAllianceZone);
    }
}
