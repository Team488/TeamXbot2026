package competition.command_groups;

import competition.general_commands.WaitForDurationCommand;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import xbot.common.command.BaseSequentialCommandGroup;

import java.util.function.Supplier;

public class DriveToNearestShootingPositionAndShootWhenReady extends BaseSequentialCommandGroup {

    @AssistedInject
    public DriveToNearestShootingPositionAndShootWhenReady(
            DriveToShootingPositionCommand driveToShootingPositionCommand,
            WaitForHoodAndShooterToBeAtGoalCommandGroup waitForHoodAndShooterToBeAtGoalCommandGroup,
            RunCollectorHopperFeederCommandGroup runCollectorHopperFeederCommandGroup,
            @Assisted Supplier<Double> shootingTimeoutSeconds) {
        this.addCommands(driveToShootingPositionCommand);

        var fireWhenShooterAndHoodReady = waitForHoodAndShooterToBeAtGoalCommandGroup
                .andThen(new WaitForDurationCommand(shootingTimeoutSeconds).deadlineFor(runCollectorHopperFeederCommandGroup));

        this.addCommands(fireWhenShooterAndHoodReady);
    }

    @AssistedFactory
    public interface Factory {
        DriveToNearestShootingPositionAndShootWhenReady create(Supplier<Double> shootingTimeoutSeconds);
    }
}
