package competition.subsystems.shooter_feeder.commands;

import competition.subsystems.shooter.ShooterSubsystem;
import xbot.common.command.BaseSetpointCommand;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.RPM;

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
        shooter.setTargetValue(RPM.of(speed));
    }

    @Override
    public void end(boolean interrupted) {
    }
}