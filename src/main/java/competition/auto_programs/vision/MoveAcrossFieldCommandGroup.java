package competition.auto_programs.vision;

import competition.auto_programs.BaseAutonomousSequentialCommandGroup;
import competition.command_groups.DriveFromNeutralZoneToAllianceAndShootCommandGroupFactory;
import competition.command_groups.DriveToNeutralZoneAndDeployIntakeCommandGroupFactory;
import competition.subsystems.intake_deploy.commands.IntakeDeployAutoCalibrateCommandFactory;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

import javax.inject.Inject;
import javax.inject.Provider;

public class MoveAcrossFieldCommandGroup extends BaseAutonomousSequentialCommandGroup {

    @Inject
    public MoveAcrossFieldCommandGroup(AutonomousCommandSelector autoSelector,
            Provider<DriveToNeutralZoneAndDeployIntakeCommandGroupFactory> driveToNeutralZoneAndDeployIntakeCommandProvider,
            Provider<DriveFromNeutralZoneToAllianceAndShootCommandGroupFactory> driveFromNeutralZoneToAllianceAndShootCommandGroupProvider,
            IntakeDeployAutoCalibrateCommandFactory intakeDeployAutoCalibrateCommandFactory) {
        super(autoSelector);

        getAutoStatusChangeCommand("Starting MoveAcrossFieldCommandGroup");
        // Force intake to calibrate if it's not calibrated yet.
        var intakeCalibrationCommand = intakeDeployAutoCalibrateCommandFactory.create();
        var driveToNeutralZone = driveToNeutralZoneAndDeployIntakeCommandProvider.get().create()
                .alongWith(getAutoStatusChangeCommand("Driving to neutral zone and back"));

        var calibrateAndDrive = new ParallelCommandGroup(intakeCalibrationCommand, driveToNeutralZone);

        this.addCommands(calibrateAndDrive);

        var driveToAllianceZone = driveFromNeutralZoneToAllianceAndShootCommandGroupProvider.get().create()
                .alongWith(getAutoStatusChangeCommand("Driving to alliance and shoot"));

        this.addCommands(driveToAllianceZone);
    }
}
