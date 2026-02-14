package competition.subsystems.shooter_feeder.commands;

import competition.subsystems.shooter_feeder.ShooterFeederSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class ShooterFeederStop extends BaseCommand {
    final ShooterFeederSubsystem shooterFeeder;

    @Inject
    public ShooterFeederStop (ShooterFeederSubsystem shooterFeeder) {
        this.shooterFeeder = shooterFeeder;
        this.addRequirements(shooterFeeder);
    }

    @Override
    public void initialize() {
        shooterFeeder.stop();
    }
}