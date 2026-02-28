package competition.subsystems.climber.commands;

import competition.subsystems.climber.ClimberSubsystem;
import edu.wpi.first.units.measure.Angle;
import xbot.common.command.BaseSetpointCommand;

import javax.inject.Inject;

public class ClimberSetPointCommand extends BaseSetpointCommand {
    private final ClimberSubsystem climber;
    private Angle goalAngle;

    @Inject
    public ClimberSetPointCommand(ClimberSubsystem climber) {
        super(climber);
        this.climber = climber;
    }

    public void setGoalAngle(Angle goalAngle) {
        this.goalAngle = goalAngle;
    }

    @Override
    public void initialize() {
        if (goalAngle != null) {
            climber.setTargetValue(goalAngle);
        } else {
            climber.setTargetValue(climber.getCurrentValue());
        }
    }
}
