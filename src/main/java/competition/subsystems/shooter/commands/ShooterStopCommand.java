package competition.subsystems.shooter.commands;

import competition.subsystems.shooter.ShooterSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class ShooterStopCommand extends BaseCommand {
    final ShooterSubsystem shooter;
    private double targetVelocity = 0;

    @Inject
    public ShooterStopCommand(ShooterSubsystem shooterSubsystem) {
        shooter = shooterSubsystem;
    }

    public void setTargetVelocity(double targetVelocity) {
        this.targetVelocity = targetVelocity;
    }

    @Override
    public void initialize() {
        shooter.setTargetVelocity(targetVelocity);
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
