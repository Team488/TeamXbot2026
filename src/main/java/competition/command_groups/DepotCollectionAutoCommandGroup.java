package competition.command_groups;

import competition.subsystems.drive.commands.DepotCollectionAutoCommand;
import competition.subsystems.drive.commands.DriveToDepotAutoCommand;
import competition.subsystems.pose.Landmarks;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

public class DepotCollectionAutoCommandGroup extends SequentialCommandGroup {
    public DepotCollectionAutoCommandGroup(DriveToDepotAutoCommand driveToDepotAutoCommand,
                                           DepotCollectionAutoCommand depotCollectionAutoCommand,
                                           HopperAndIntakeCommandGroup hopperAndIntakeCommandGroup,
                                           PoseSubsystem pose) {
        addCommands(
                pose.createSetPositionCommand(PoseSubsystem.convertBlueToRedIfNeeded(Landmarks.blueStartTrenchToDepot)),
                driveToDepotAutoCommand,
                hopperAndIntakeCommandGroup,
                depotCollectionAutoCommand
        );
    }
}
