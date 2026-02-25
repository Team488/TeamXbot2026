package competition.subsystems.climber.commands;

import competition.subsystems.climber.ClimberSubsystem;
import edu.wpi.first.wpilibj2.command.Command;
import xbot.common.command.BaseCommand;
import xbot.common.command.SimpleWaitForMaintainerCommand;
import xbot.common.properties.DoubleProperty;

import javax.inject.Inject;

public class ClimberRetractCommand extends BaseCommand {
    ClimberSubsystem climber;

    public DoubleProperty readinessTimeoutSeconds;

    @Inject
    public ClimberRetractCommand(ClimberSubsystem climberSubsystem) {
        climber = climberSubsystem;
        this.addRequirements(climber);
    }

    @Override
    public void initialize() {
        climber.retract();
        log.info("Initialized ClimberRetract");
    }

    public Command getWaitForAtGoalCommand() {
        return new SimpleWaitForMaintainerCommand(this, () -> readinessTimeoutSeconds.get());
    }
}
