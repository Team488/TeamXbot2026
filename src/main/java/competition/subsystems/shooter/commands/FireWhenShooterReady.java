package competition.subsystems.shooter.commands;

import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.shooter_feeder.ShooterFeederSubsystem;
import xbot.common.command.BaseCommand;

public class FireWhenShooterReady extends BaseCommand {

    ShooterSubsystem shooter;
    ShooterFeederSubsystem feeder;

    public FireWhenShooterReady (ShooterSubsystem shooterSubsystem, ShooterFeederSubsystem shooterFeederSubsystem) {
        this.shooter = shooterSubsystem;
        this.feeder = shooterFeederSubsystem;
    }

    @Override
    public void initialize() {
        log.info("Initializing...");
    }

    @Override
    public void execute() {
        if (shooter.isReadyToFire()) {
            feeder.fire();
        }
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


