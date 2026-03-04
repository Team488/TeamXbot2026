package competition.auto_programs.vision;

import competition.auto_programs.BaseAutonomousSequentialCommandGroup;
import competition.command_groups.DriveToNeutralZoneAndDeployIntakeCommandGroupFactory;
import competition.subsystems.pose.Landmarks;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

import javax.inject.Inject;
import javax.inject.Provider;

public class MoveAcrossFieldCommandGroup extends BaseAutonomousSequentialCommandGroup {

    @Inject
    public MoveAcrossFieldCommandGroup(AutonomousCommandSelector autoSelector,
            Provider<DriveToNeutralZoneAndDeployIntakeCommandGroupFactory> driveToNeutralZoneAndDeployIntakeCommandProvider) {
        super(autoSelector);

        getAutoStatusChangeCommand("Starting MoveAcrossFieldCommandGroup");
        var driveToNeutralZone =
            driveToNeutralZoneAndDeployIntakeCommandProvider.get().create()
            .alongWith(getAutoStatusChangeCommand("Driving to neutral zone"));

        this.addCommands(driveToNeutralZone);
    }
}
