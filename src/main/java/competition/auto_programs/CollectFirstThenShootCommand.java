package competition.auto_programs.vision;

import competition.auto_programs.BaseAutonomousSequentialCommandGroup;
import competition.command_groups.FireWhenReadyAndRetractIntakeDeployCommandGroup;
import competition.command_groups.NoWaitFinishedShootingCommand;
import competition.command_groups.PrepareToShootCommandGroup;
import competition.command_groups.DriveFromNeutralZoneToAllianceAndShootCommandGroupFactory;
import competition.command_groups.DriveToNeutralZoneAndDeployIntakeCommandGroupFactory;
import competition.subsystems.pose.TrajectoriesCalculation;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

import javax.inject.Inject;
import javax.inject.Provider;

public class CollectFirstThenShootCommand extends BaseAutonomousSequentialCommandGroup {

    @Inject
    public CollectFirstThenShootCommand(AutonomousCommandSelector autoSelector,
           TrajectoriesCalculation trajectoriesCalculation,
           FireWhenReadyAndRetractIntakeDeployCommandGroup fireWhenShooterAndRetractIntake,
           PrepareToShootCommandGroup prepareToShootCommandGroup,
           Provider<DriveToNeutralZoneAndDeployIntakeCommandGroupFactory> driveToNeutralZoneAndDeployIntakeCommandProvider,
           Provider<DriveFromNeutralZoneToAllianceAndShootCommandGroupFactory> driveFromNeutralZoneToAllianceAndShootCommandGroupProvider,
                                                   Provider<NoWaitFinishedShootingCommand> finishedShootingCommandProvider,
           PropertyFactory pf) {
        super(autoSelector);

        pf.setPrefix(this.getName());

        getAutoStatusChangeCommand("Starting CollectFirstThenShootCommand");
        prepareToShootCommandGroup.setPresetLocation(TrajectoriesCalculation.PresetShootingDistance.TRENCH);

        var driveToNeutralZone = driveToNeutralZoneAndDeployIntakeCommandProvider.get().create()
                .alongWith(getAutoStatusChangeCommand("Driving to neutral zone and back"));

        this.addCommands(driveToNeutralZone);

        var firstShotTimeoutProperty = pf.createPersistentProperty("First shot timeout seconds", 3.0);
        var driveToAllianceZoneAndShoot = driveFromNeutralZoneToAllianceAndShootCommandGroupProvider.get()
                        .create(firstShotTimeoutProperty::get)
                        .alongWith(getAutoStatusChangeCommand("Driving to alliance and shoot"));
        this.addCommands(driveToAllianceZoneAndShoot);

        var driveToNeutralZone2 = driveToNeutralZoneAndDeployIntakeCommandProvider.get().create()
                .alongWith(getAutoStatusChangeCommand("Driving to neutral zone and back"));

        this.addCommands(driveToNeutralZone2);

        var driveToAllianceZoneAndShoot2 = driveFromNeutralZoneToAllianceAndShootCommandGroupProvider.get().create()
                .alongWith(getAutoStatusChangeCommand("Driving to alliance and shoot"));
        this.addCommands(driveToAllianceZoneAndShoot2);


        
    }
}
