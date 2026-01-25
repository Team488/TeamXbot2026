package competition.auto;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.drive.commands.DriveToOutpostCommand;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.command.BaseSequentialCommandGroup;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

public class DriveToOutpostAuto extends BaseSequentialCommandGroup {
    public DriveToOutpostAuto(AutonomousCommandSelector commandSelector,
                              DriveSubsystem driveSubsystem,
                              DriveToOutpostCommand driveToOutpostCommand, PoseSubsystem pose) {
        final AutonomousCommandSelector autonomousCommandSelector;

        this.addCommands(driveToOutpostCommand);
    }

}
