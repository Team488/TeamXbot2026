package competition.auto_programs;

import competition.subsystems.pose.Landmarks;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

public class BaseAutonomousSequentialCommandGroup extends SequentialCommandGroup {

    final AutonomousCommandSelector autoSelector;

    public BaseAutonomousSequentialCommandGroup(AutonomousCommandSelector autoSelector) {
        this.autoSelector = autoSelector;
    }

    public Command getAutoStatusChangeCommand(String message) {
        return autoSelector.createAutonomousStateMessageCommand(message);
    }
}
