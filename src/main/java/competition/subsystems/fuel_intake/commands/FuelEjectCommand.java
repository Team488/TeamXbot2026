package competition.subsystems.fuel_intake.commands;

import competition.subsystems.fuel_intake.IntakeSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class FuelEjectCommand extends BaseCommand {
    IntakeSubsystem fuelEject;

    @Inject
    public FuelEjectCommand (IntakeSubsystem intakeSubsystem) {
        fuelEject = intakeSubsystem ;
        this.addRequirements(fuelEject);
    }

    @Override
    public void initialize() {
        fuelEject.eject();
    }
}