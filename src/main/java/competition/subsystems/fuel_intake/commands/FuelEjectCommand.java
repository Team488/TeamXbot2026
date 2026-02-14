package competition.subsystems.fuel_intake.commands;

import competition.subsystems.fuel_intake.IntakeSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class FuelEjectCommand extends BaseCommand {
    final IntakeSubsystem intakeSubsystem;

    @Inject
    public FuelEjectCommand(IntakeSubsystem intakeSubsystem) {
        this.intakeSubsystem = intakeSubsystem ;
        this.addRequirements(this.intakeSubsystem);
    }

    @Override
    public void initialize() {
        intakeSubsystem.eject();
    }

    @Override
    public void execute() {
        log.info("Ejecting fuel!");
    }
}