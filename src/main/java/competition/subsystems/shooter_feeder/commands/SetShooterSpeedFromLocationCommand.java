package competition.subsystems.shooter_feeder.commands;

import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.shooter.ShooterSubsystem.FieldScoringLocation;
import xbot.common.command.BaseSetpointCommand;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.RPM;

public class SetShooterSpeedFromLocationCommand extends BaseSetpointCommand {
    private final ShooterSubsystem shooter;
    private FieldScoringLocation currentLocation = FieldScoringLocation.Min_Distance;

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
        double speed = shooter.getRPMForScoringLocation(currentLocation);
        shooter.setTargetValue(RPM.of(speed));
    }

    @Override
    public void execute() {
        double speed = shooter.getRPMForScoringLocation(currentLocation);
        shooter.setTargetValue(RPM.of(speed));
        shooter.runMotorsAtVelocity(shooter.getTrimmedTargetValue());
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