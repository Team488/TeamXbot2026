package competition.subsystems.shooter.commands;

import xbot.common.command.BaseCommand;
import competition.subsystems.shooter.ShooterSubsystem;

import javax.inject.Inject;

public class LowPowerModeOnCommand extends BaseCommand{

    final ShooterSubsystem shooter;

    @Inject
    public LowPowerModeOnCommand(ShooterSubsystem shooterSubsystem, ShooterSubsystem shooter) {
        this.shooter = shooter;
    }

    public void initialize() {
        shooter.setLowPowerMode(true);
    }
    @Override
    public boolean isFinished() {
        return true;
    }
}
