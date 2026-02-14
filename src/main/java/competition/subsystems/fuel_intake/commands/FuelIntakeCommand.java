package competition.subsystems.fuel_intake.commands;

import competition.subsystems.fuel_intake.IntakeSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class FuelIntakeCommand extends BaseCommand {
    final IntakeSubsystem intakeSubsystem;

    @Inject
    public FuelIntakeCommand(IntakeSubsystem intakeSubsystem) {
        this.intakeSubsystem = intakeSubsystem ;
        this.addRequirements(this.intakeSubsystem);
    }

    @Override
    public void initialize() {
        intakeSubsystem.intake();
        log.info("Initialized FuelIntake");
    }
}
