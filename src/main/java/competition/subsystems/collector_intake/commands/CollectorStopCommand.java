package competition.subsystems.collector_intake.commands;

import competition.subsystems.collector_intake.CollectorSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class CollectorStopCommand extends BaseCommand {
    final CollectorSubsystem collectorSubsystem;

    @Inject
    public CollectorStopCommand(CollectorSubsystem collectorSubsystem) {
        this.collectorSubsystem = collectorSubsystem ;
        this.addRequirements(this.collectorSubsystem);
    }

    @Override
    public void initialize() {
        super.initialize();
        collectorSubsystem.stop();
    }
}
