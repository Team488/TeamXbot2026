package competition.subsystems.shooter_feeder.commands;

import competition.subsystems.shooter_feeder.ShooterFeederSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class ShooterFeederFireVelocity extends BaseCommand {

    public final ShooterFeederSubsystem shooterFeeder;

    @Inject
    public ShooterFeederFireVelocity (ShooterFeederSubsystem shooterFeeder) {
        this.shooterFeeder = shooterFeeder;
        this.addRequirements(shooterFeeder);
    }

    public void initialize() {
        shooterFeeder.fireVelocity();
    }
}
