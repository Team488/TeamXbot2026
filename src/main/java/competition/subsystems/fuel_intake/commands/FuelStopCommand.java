package competition.subsystems.fuel_intake.commands;

import competition.subsystems.fuel_intake.IntakeSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class FuelStopCommand extends BaseCommand {
    final IntakeSubsystem intakeSubsystem;

    @Inject
    public FuelStopCommand(IntakeSubsystem intakeSubsystem) {
        this.intakeSubsystem = intakeSubsystem ;
        this.addRequirements(this.intakeSubsystem);
    }

    @Override
    public void initialize() {
        intakeSubsystem.stop();
    }
}
