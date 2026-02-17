package competition.subsystems.climber.commands;

import competition.subsystems.climber.ClimberSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class ClimberExtendCommand extends BaseCommand {
    ClimberMaintainer climber;

    @Inject
    public ClimberExtendCommand(ClimberMaintainer climberMaintainer) {
        climber = climberMaintainer;
        this.addRequirements(climber);
    }

    @Override
    public void initialize() {
        climber.extend();
        log.info("Initialized ClimberExtend");
    }

}
