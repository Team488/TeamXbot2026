package competition.subsystems.shooter_feeder.commands;

import competition.subsystems.shooter_feeder.ShooterFeederSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class DisableShooterFeederCommand extends BaseCommand {
    final ShooterFeederSubsystem shooterFeeder;

    @Inject
    public DisableShooterFeederCommand(ShooterFeederSubsystem shooterFeeder) {
        this.shooterFeeder = shooterFeeder;
        this.addRequirements(shooterFeeder);
    }

    @Override
    public void initialize() {
        // TODO: Fill in later
    }
}