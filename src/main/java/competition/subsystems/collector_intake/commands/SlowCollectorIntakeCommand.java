package competition.subsystems.collector_intake.commands;

import competition.subsystems.collector_intake.CollectorSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class SlowCollectorIntakeCommand extends BaseCommand {
    final CollectorSubsystem collectorSubsystem;

    @Inject
    public SlowCollectorIntakeCommand(CollectorSubsystem collectorSubsystem) {
        this.collectorSubsystem = collectorSubsystem;
        this.addRequirements(this.collectorSubsystem);
    }

    @Override
    public void initialize() {
        super.initialize();
        collectorSubsystem.intakeVelocityWhileShooting();
        log.info("Initialized Slow FuelIntake");
    }
}
