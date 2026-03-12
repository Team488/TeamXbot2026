package competition.auto_programs;

import competition.auto_programs.BaseAutonomousSequentialCommandGroup;
import competition.command_groups.FireWhenReadyShooterCommandGroup;
import competition.command_groups.PrepareToShootCommandGroup;
import competition.subsystems.intake_deploy.commands.IntakeDeployAutoCalibrateCommandFactory;
import competition.subsystems.pose.TrajectoriesCalculation;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

import javax.inject.Inject;

public class ShootFromHubCommandGroup extends BaseAutonomousSequentialCommandGroup {

    @Inject
    public ShootFromHubCommandGroup(AutonomousCommandSelector autoSelector,
            TrajectoriesCalculation trajectoriesCalculation,
            FireWhenReadyShooterCommandGroup fireWhenReadyShooterCommandGroup,
            PrepareToShootCommandGroup prepareToShootCommandGroup,
            IntakeDeployAutoCalibrateCommandFactory intakeDeployAutoCalibrateCommandFactory) {
        super(autoSelector);

        getAutoStatusChangeCommand("Starting ShootFromHubCommandGroup");
        var intakeCalibrationCommand = intakeDeployAutoCalibrateCommandFactory.create();
        prepareToShootCommandGroup.setPresetLocation(TrajectoriesCalculation.PresetShootingDistance.NEAR);
        var calibrateAndShoot = new ParallelCommandGroup(intakeCalibrationCommand, prepareToShootCommandGroup);

        this.addCommands(calibrateAndShoot);
        this.addCommands(fireWhenReadyShooterCommandGroup);
    }
}
