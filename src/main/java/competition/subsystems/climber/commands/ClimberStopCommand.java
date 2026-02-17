package competition.subsystems.climber.commands;

import competition.subsystems.climber.ClimberSubsystem;
import edu.wpi.first.wpilibj2.command.Subsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class ClimberStopCommand extends BaseCommand {
    ClimberSubsystem climber;

    @Inject
    public ClimberStopCommand(ClimberSubsystem climberSubsystem) {
        climber = climberSubsystem;
        this.addRequirements((Subsystem) climber);
    }

    @Override
    public void initialize() {
        climber.stop();
    }
}
