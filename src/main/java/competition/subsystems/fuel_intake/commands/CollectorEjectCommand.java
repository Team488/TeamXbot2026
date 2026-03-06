package competition.subsystems.fuel_intake.commands;

import competition.subsystems.fuel_intake.CollectorSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class CollectorEjectCommand extends BaseCommand {
    final CollectorSubsystem collectorSubsystem;

    @Inject
    public CollectorEjectCommand(CollectorSubsystem collectorSubsystem) {
        this.collectorSubsystem = collectorSubsystem ;
        this.addRequirements(this.collectorSubsystem);
    }

    @Override
    public void initialize() {
        collectorSubsystem.eject();
        log.info("Initialized FuelEject");
    }
}