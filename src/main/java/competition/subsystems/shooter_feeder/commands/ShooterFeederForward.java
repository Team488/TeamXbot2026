package competition.subsystems.shooter_feeder.commands;

import competition.subsystems.shooter_feeder.ShooterFeederSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class ShooterFeederForward extends BaseCommand {
    final ShooterFeederSubsystem shooterFeeder;

    @Inject
    public ShooterFeederForward (ShooterFeederSubsystem shooterFeeder) {
        this.shooterFeeder = shooterFeeder;
        this.addRequirements(shooterFeeder);
    }

    @Override
    public void initialize() {
        shooterFeeder.forward();
    }
}