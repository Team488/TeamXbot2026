package competition.auto_programs;

import competition.command_groups.FireWhenShooterAndHoodReadyUntilDone;
import competition.command_groups.PrepareToShootCommandGroup;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.TrajectoriesCalculation;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.RunCommand;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

import javax.inject.Inject;

public class ShootFromTrenchCommandGroup extends BaseAutonomousSequentialCommandGroup {

    private final DoubleProperty timeout;

    @Inject
    public ShootFromTrenchCommandGroup(AutonomousCommandSelector autoSelector,
           TrajectoriesCalculation trajectoriesCalculation,
           FireWhenShooterAndHoodReadyUntilDone fireWhenShooterAndHoodReady,
           PrepareToShootCommandGroup prepareToShootCommandGroup,
           DriveSubsystem driveSubsystem,
           PropertyFactory pf) {
        super(autoSelector);

        pf.setPrefix(this.getName());
        this.timeout = pf.createPersistentProperty("TimeoutSeconds", 5.0);

        getAutoStatusChangeCommand("Starting ShootFromTrenchCommandGroup");
        prepareToShootCommandGroup.setPresetLocation(TrajectoriesCalculation.PresetShootingDistance.TRENCH);

        // run a no-op command that requires the drive subsystem so the robot doesn't move for any reason
        var holdPosition = new RunCommand(() -> {}, driveSubsystem);
        var prepareAndShoot = new ParallelCommandGroup(prepareToShootCommandGroup, fireWhenShooterAndHoodReady, holdPosition)
                .withTimeout(timeout.get());

        this.addCommands(prepareAndShoot);
    }
}
