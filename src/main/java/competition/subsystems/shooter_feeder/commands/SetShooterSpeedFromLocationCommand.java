package competition.subsystems.shooter_feeder.commands;

import competition.subsystems.shooter.ShooterSubsystem;
import xbot.common.command.BaseSetpointCommand;

import javax.inject.Inject;

public class SetShooterSpeedFromLocationCommand extends BaseSetpointCommand {
    private final ShooterSubsystem shooter;

    @Inject
    public SetShooterSpeedFromLocationCommand(ShooterSubsystem shooter) {
        super(shooter);
        this.shooter = shooter;
    }

    @Override
    public void initialize() {
        log.info("Initializing SetShooterSpeedFromLocationCommand");
    }

    @Override
    public void execute() {
        double speed = shooter.getSpeedForRange();
        shooter.setTargetValue(speed);
    }

    @Override
    public void end(boolean interrupted) {
    }
}