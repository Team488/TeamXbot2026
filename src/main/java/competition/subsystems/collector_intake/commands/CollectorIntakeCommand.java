package competition.subsystems.collector_intake.commands;

import competition.subsystems.collector_intake.CollectorSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class CollectorIntakeCommand extends BaseCommand {
    final CollectorSubsystem collectorSubsystem;

    @Inject
    public CollectorIntakeCommand(CollectorSubsystem collectorSubsystem) {
        this.collectorSubsystem = collectorSubsystem ;
        this.addRequirements(this.collectorSubsystem);
    }

    @Override
    public void initialize() {
        super.initialize();
        collectorSubsystem.intake();
        log.info("Initialized FuelIntake");
    }
}
