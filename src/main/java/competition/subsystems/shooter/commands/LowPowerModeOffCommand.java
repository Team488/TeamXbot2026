package competition.subsystems.shooter.commands;

import competition.subsystems.shooter.ShooterSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class LowPowerModeOffCommand extends BaseCommand {
    final ShooterSubsystem shooter;

    @Inject
    public LowPowerModeOffCommand(ShooterSubsystem shooterSubsystem, ShooterSubsystem shooter) {
        this.shooter = shooter;
    }

    public void initialize() {
        shooter.setLowPowerMode(false);
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
