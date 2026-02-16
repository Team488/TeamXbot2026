package competition.subsystems.shooter_feeder.commands;

import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.shooter_feeder.ShooterFeederSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class ShooterFeederVelocity extends BaseCommand {

    public final ShooterFeederSubsystem shooterFeeder;

    @Inject
    public ShooterFeederVelocity (ShooterFeederSubsystem shooterFeeder) {
        this.shooterFeeder = shooterFeeder;
        this.addRequirements(shooterFeeder);
    }

    public void initialize() {
        shooterFeeder.fireVelocity();
    }
}
