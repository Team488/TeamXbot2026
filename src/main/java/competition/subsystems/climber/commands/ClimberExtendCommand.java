package competition.subsystems.climber.commands;

import competition.subsystems.climber.ClimberSubsystem;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import xbot.common.command.BaseCommand;
import xbot.common.command.BaseSetpointCommand;

import javax.inject.Inject;

public class ClimberExtendCommand extends BaseSetpointCommand {
     ClimberSubsystem climber;

    @Inject
     public ClimberExtendCommand(ClimberSubsystem climberSubsystem) {
        super(climberSubsystem);
        this.climber = climberSubsystem;
        this.addRequirements(climber);
    }

    @Override
    public SequentialCommandGroup andThen(Command... unhook) {
        return super.andThen(unhook);
    }

    @Override
    public void initialize() {
        climber.extend();
        log.info("Initialized ClimberExtend");
    }
}
