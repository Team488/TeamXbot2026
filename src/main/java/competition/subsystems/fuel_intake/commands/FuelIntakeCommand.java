package competition.subsystems.fuel_intake.commands;

import competition.subsystems.fuel_intake.IntakeSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class FuelIntakeCommand extends BaseCommand {
    IntakeSubsystem fuelIntake;

    @Inject
    public FuelIntakeCommand (IntakeSubsystem intakeSubsystem) {
        fuelIntake = intakeSubsystem ;
        this.addRequirements(fuelIntake);
    }

    @Override
    public void initialize() {
        fuelIntake.intake();
    }
}
