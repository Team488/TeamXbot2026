package competition.auto_programs;

import competition.auto_programs.BaseAutonomousSequentialCommandGroup;
import competition.command_groups.FireWhenReadyShooterCommandGroup;
import competition.command_groups.PrepareToShootCommandGroup;
import competition.subsystems.intake_deploy.commands.IntakeDeployAutoCalibrateCommandFactory;
import competition.subsystems.pose.TrajectoriesCalculation;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

import javax.inject.Inject;

public class ShootFromTrenchCommandGroup extends BaseAutonomousSequentialCommandGroup {

    @Inject
    public ShootFromTrenchCommandGroup(AutonomousCommandSelector autoSelector,
            TrajectoriesCalculation trajectoriesCalculation,
            FireWhenReadyShooterCommandGroup fireWhenReadyShooterCommandGroup,
            PrepareToShootCommandGroup prepareToShootCommandGroup,
            IntakeDeployAutoCalibrateCommandFactory intakeDeployAutoCalibrateCommandFactory) {
        super(autoSelector);

        getAutoStatusChangeCommand("Starting ShootFromTrenchCommandGroup");
        var intakeCalibrationCommand = intakeDeployAutoCalibrateCommandFactory.create();
        prepareToShootCommandGroup.setPresetLocation(TrajectoriesCalculation.PresetShootingDistance.TRENCH);
        var calibrateAndShoot = new ParallelCommandGroup(intakeCalibrationCommand, prepareToShootCommandGroup, fireWhenReadyShooterCommandGroup);

        this.addCommands(calibrateAndShoot);
    }
}
