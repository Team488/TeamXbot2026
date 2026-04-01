package competition.auto_programs.vision;

import javax.inject.Inject;
import javax.inject.Provider;

import competition.auto_programs.BaseAutonomousSequentialCommandGroup;
import competition.command_groups.JustDriveFullNeutralZoneAndShootMovementCommand;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

public class JustDriveNeutralMoveCommand extends BaseAutonomousSequentialCommandGroup {
    @Inject
    public JustDriveNeutralMoveCommand(AutonomousCommandSelector autoSelector,
            Provider<JustDriveFullNeutralZoneAndShootMovementCommand> justDriveFullNeutralZoneAndShootMovementCommand) {
        super(autoSelector);

        getAutoStatusChangeCommand("Starting MoveAcrossFieldCommandGroup");

        var justDrive = justDriveFullNeutralZoneAndShootMovementCommand.get()
                .alongWith(getAutoStatusChangeCommand("Just drive full neutral zone movement"));
        this.addCommands(justDrive);
    }
}
