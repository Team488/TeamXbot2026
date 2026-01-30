package competition.subsystems.shooter.commands;

import competition.subsystems.shooter.ShooterSubsystem;
import xbot.common.command.BaseCommand;

public class FireWhenShooterReady extends BaseCommand {

    ShooterSubsystem shooter;

    public FireWhenShooterReady (ShooterSubsystem shooterSubsystem) {
        this.shooter = shooterSubsystem;

    }

    @Override
    public void initialize() {}

    @Override
    public void execute() {
        shooter.runAtTargetVelocity();
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public void end(boolean interrupted) {
        log.info("Ending");
        super.end(interrupted);
    }
}
