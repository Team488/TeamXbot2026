package competition.subsystems.climber.commands;

import competition.subsystems.climber.ClimberSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class ClimberExtendCommand extends BaseCommand {
    ClimberSubsystem climber;

    @Inject
    public ClimberExtendCommand(ClimberSubsystem climberSubsystem) {
        climber = climberSubsystem;
        this.addRequirements(climber);
    }

    @Override
    public void initialize() {
        climber.setPower(climber.extendPower);
        log.info("Initialized ClimberRetract");
    }
}
