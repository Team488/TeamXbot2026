package competition.subsystems.fuel_intake.commands;

import competition.subsystems.fuel_intake.IntakeSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class FuelStopCommand extends BaseCommand {
    IntakeSubsystem fuelStop;

    @Inject
    public FuelStopCommand (IntakeSubsystem intakeSubsystem) {
        fuelStop = intakeSubsystem ;
        this.addRequirements(fuelStop);
    }

    @Override
    public void initialize() {
        fuelStop.stop();
    }

}
