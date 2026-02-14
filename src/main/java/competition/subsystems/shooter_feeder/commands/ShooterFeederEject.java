package competition.subsystems.shooter_feeder.commands;

import competition.subsystems.shooter_feeder.ShooterFeederSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class ShooterFeederEject extends BaseCommand {
    final ShooterFeederSubsystem shooterFeeder;

    @Inject
    public ShooterFeederEject (ShooterFeederSubsystem shooterFeeder) {
        this.shooterFeeder = shooterFeeder;
        this.addRequirements(shooterFeeder);
    }

    @Override
    public void initialize() {
        shooterFeeder.eject();
        log.info("Initialized ShooterFeederEject");
    }
}