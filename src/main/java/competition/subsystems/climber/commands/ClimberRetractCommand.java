package competition.subsystems.climber.commands;

import competition.subsystems.climber.ClimberSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class ClimberRetractCommand extends BaseCommand {
    ClimberSubsystem climber;

    @Inject
    public ClimberRetractCommand(ClimberSubsystem climberSubsystem) {
        climber = climberSubsystem;
        this.addRequirements(climber);
    }

    @Override
    public void initialize() {
        climber.setPower(climber.retractPower);
        log.info("Initialized ClimberRetract");
    }
}
