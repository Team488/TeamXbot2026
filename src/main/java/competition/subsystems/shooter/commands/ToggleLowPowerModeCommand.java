package competition.subsystems.shooter.commands;

import xbot.common.command.BaseCommand;
import competition.subsystems.shooter.ShooterSubsystem;

import javax.inject.Inject;

public class ToggleLowPowerModeCommand extends BaseCommand{

    final ShooterSubsystem shooter;

    @Inject
    public ToggleLowPowerModeCommand(ShooterSubsystem shooterSubsystem, ShooterSubsystem shooter) {
        this.shooter = shooter;
    }

    public void initialize() {
        if (!shooter.isInLowPowerMode) {
            shooter.lowPowerMode();
            shooter.isInLowPowerMode = true;
        } else  {
            shooter.isInLowPowerMode = false;
        }
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
