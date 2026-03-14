package competition.auto_programs;

import competition.auto_programs.BaseAutonomousSequentialCommandGroup;
import competition.command_groups.FireWhenReadyShooterCommandGroup;
import competition.command_groups.PrepareToShootCommandGroup;
import competition.subsystems.intake_deploy.commands.IntakeDeployAutoCalibrateCommandFactory;
import competition.subsystems.pose.TrajectoriesCalculation;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

import javax.inject.Inject;

public class ShootFromTrenchCommandGroup extends BaseAutonomousSequentialCommandGroup {

    private final DoubleProperty timeout;

    @Inject
    public ShootFromTrenchCommandGroup(AutonomousCommandSelector autoSelector,
           TrajectoriesCalculation trajectoriesCalculation,
           FireWhenReadyShooterCommandGroup fireWhenReadyShooterCommandGroup,
           PrepareToShootCommandGroup prepareToShootCommandGroup,
           IntakeDeployAutoCalibrateCommandFactory intakeDeployAutoCalibrateCommandFactory,
           PropertyFactory pf) {
        super(autoSelector);

        pf.setPrefix(this.getName());
        this.timeout = pf.createPersistentProperty("TimeoutSeconds", 5.0);

        getAutoStatusChangeCommand("Starting ShootFromTrenchCommandGroup");
        var intakeCalibrationCommand = intakeDeployAutoCalibrateCommandFactory.create();
        prepareToShootCommandGroup.setPresetLocation(TrajectoriesCalculation.PresetShootingDistance.TRENCH);
        var calibrateAndShoot = new ParallelCommandGroup(intakeCalibrationCommand, prepareToShootCommandGroup, fireWhenReadyShooterCommandGroup)
                .withTimeout(timeout.get());

        this.addCommands(calibrateAndShoot);
    }
}
