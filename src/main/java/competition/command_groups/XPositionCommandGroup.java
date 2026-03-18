package competition.command_groups;

import xbot.common.command.BaseParallelCommandGroup;
import xbot.common.command.NamedInstantCommand;
import xbot.common.subsystems.drive.BaseSwerveDriveSubsystem;

import javax.inject.Inject;

public class XPositionCommandGroup extends BaseParallelCommandGroup {

    @Inject
    public XPositionCommandGroup(BaseSwerveDriveSubsystem baseSwerveDriveSubsystem) {

        //Turns the wheels into an X shape so it is harder to shove our robot
        var XPosition = new NamedInstantCommand("XPosition", baseSwerveDriveSubsystem::setWheelsToXMode);

        this.addCommands(
                XPosition
        );
    }
}