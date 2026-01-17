package competition.subsystems.shooter_feeder.commands;

import competition.subsystems.shooter_feeder.ShooterFeederSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class EnableShooterFeederCommand extends BaseCommand {
    ShooterFeederSubsystem shooterFeeder;

    @Inject
    public EnableShooterFeederCommand(ShooterFeederSubsystem shooterFeeder) {
        this.shooterFeeder = shooterFeeder;
        this.addRequirements(shooterFeeder);
    }
    @Override
    public void initialize() {

    }

    @Override
    public void end(boolean interrupted) {
    }



}

