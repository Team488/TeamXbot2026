package competition.auto_programs;

import competition.auto_programs.BaseAutonomousSequentialCommandGroup;
import competition.command_groups.FireWhenReadyShooterCommandGroup;
import competition.command_groups.PrepareToShootCommandGroup;
import competition.subsystems.pose.TrajectoriesCalculation;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

import javax.inject.Inject;
import javax.inject.Provider;

public class ShootFromTrenchCommandGroup extends BaseAutonomousSequentialCommandGroup {

    @Inject
    public ShootFromTrenchCommandGroup(AutonomousCommandSelector autoSelector,
                                       TrajectoriesCalculation trajectoriesCalculation,
                                       FireWhenReadyShooterCommandGroup fireWhenReadyShooterCommandGroup,
                                       PrepareToShootCommandGroup prepareToShootCommandGroup) {
        super(autoSelector);

        getAutoStatusChangeCommand("Starting ShootFromTrenchCommandGroup");
        prepareToShootCommandGroup.setPresetLocation(TrajectoriesCalculation.PresetShootingDistance.TRENCH);

        this.addCommands(prepareToShootCommandGroup);

        this.addCommands(fireWhenReadyShooterCommandGroup);
    }
}
