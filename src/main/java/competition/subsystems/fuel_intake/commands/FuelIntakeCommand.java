package competition.subsystems.fuel_intake.commands;

import competition.subsystems.fuel_intake.CollectorSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class FuelIntakeCommand extends BaseCommand {
    final CollectorSubsystem collectorSubsystem;

    @Inject
    public FuelIntakeCommand(CollectorSubsystem intakeSubsystem) {
        this.collectorSubsystem = intakeSubsystem ;
        this.addRequirements(this.collectorSubsystem);
    }

    @Override
    public void initialize() {
        collectorSubsystem.intake();
        log.info("Initialized FuelIntake");
    }
}
