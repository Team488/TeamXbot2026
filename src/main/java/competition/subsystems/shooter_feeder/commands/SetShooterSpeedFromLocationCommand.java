package competition.subsystems.shooter_feeder.commands;

import competition.subsystems.shooter.ShooterSubsystem;
import xbot.common.command.BaseSetpointCommand;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.RPM;

import competition.subsystems.shooter.ShooterSubsystem.FieldScoringLocation;

public class SetShooterSpeedFromLocationCommand extends BaseSetpointCommand {
    private final ShooterSubsystem shooter;
    private FieldScoringLocation currentLocation = FieldScoringLocation.Point_1;

    @Inject
    public SetShooterSpeedFromLocationCommand(ShooterSubsystem shooter) {
        super(shooter);
        this.shooter = shooter;
    }

    public void setScoringLocation(FieldScoringLocation location) {
        this.currentLocation = location;
    }

    @Override
    public void initialize() {
        log.info("Initializing SetShooterSpeedFromLocationCommand at location: " + currentLocation);
        double speed = shooter.getRPMForScoringLocation(currentLocation);
        shooter.setTargetValue(speed);
    }

    @Override
    public void execute() {
        double speed = shooter.getRPMForScoringLocation(currentLocation);
        shooter.setTargetValue(speed);
    }

    @Override
    public void end(boolean interrupted) {
        if (interrupted) {
            shooter.stop();
        }
    }

    @Override
    public boolean isFinished() {
        return shooter.isMaintainerAtGoal();
    }
}