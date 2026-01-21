package competition.subsystems.shooter.commands;

import competition.subsystems.shooter.ShooterSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class ShooterStopCommand extends BaseCommand {
    ShooterSubsystem shooter;

    @Inject
    public ShooterStopCommand(ShooterSubsystem shooterSubsystem) {
        shooter = shooterSubsystem;
        this.addRequirements(shooter);
    }

    @Override
    public void initialize() {
        shooter.stop();
    }
}
