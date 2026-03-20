package competition.auto_programs.vision;

import competition.auto_programs.BaseAutonomousSequentialCommandGroup;
import competition.command_groups.FireWhenShooterReady;
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

public class ShootFromTrenchThenMoveToNeutralCommand extends BaseAutonomousSequentialCommandGroup {

    private final DoubleProperty timeout;

    @Inject
    public ShootFromTrenchThenMoveToNeutralCommand(AutonomousCommandSelector autoSelector,
           TrajectoriesCalculation trajectoriesCalculation,
           FireWhenShooterReady fireWhenShooterReady,
           PrepareToShootCommandGroup prepareToShootCommandGroup,
           Provider<DriveToNeutralZoneAndDeployIntakeCommandGroupFactory> driveToNeutralZoneAndDeployIntakeCommandProvider,
           Provider<DriveFromNeutralZoneToAllianceAndShootCommandGroupFactory> driveFromNeutralZoneToAllianceAndShootCommandGroupProvider,
                                                   Provider<NoWaitFinishedShootingCommand> finishedShootingCommandProvider,
           PropertyFactory pf) {
        super(autoSelector);

        pf.setPrefix(this.getName());
        this.timeout = pf.createPersistentProperty("TimeoutSeconds", 5.0);

        getAutoStatusChangeCommand("Starting ShootFromTrenchThenMoveToNeutralCommand");
        prepareToShootCommandGroup.setPresetLocation(TrajectoriesCalculation.PresetShootingDistance.TRENCH);

        var calibrateAndShoot = new ParallelCommandGroup(prepareToShootCommandGroup, fireWhenShooterReady)
                .withTimeout(timeout.get());

        this.addCommands(calibrateAndShoot);
        this.addCommands(finishedShootingCommandProvider.get().alongWith(getAutoStatusChangeCommand("Running finished shooting command.")));

        var driveToNeutralZone = driveToNeutralZoneAndDeployIntakeCommandProvider.get().create()
                .alongWith(getAutoStatusChangeCommand("Driving to neutral zone and back"));

        this.addCommands(driveToNeutralZone);

        var driveToAllianceZone = driveFromNeutralZoneToAllianceAndShootCommandGroupProvider.get().create()
                .alongWith(getAutoStatusChangeCommand("Driving to alliance and shoot"));
        this.addCommands(driveToAllianceZone);
    }
}
