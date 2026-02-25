package competition.subsystems.shooter.commands;

import competition.subsystems.shooter.ShooterSubsystem;
import edu.wpi.first.units.measure.AngularVelocity;
import xbot.common.command.BaseSetpointCommand;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.RPM;

public class ShooterOutputCommand extends BaseSetpointCommand {
    private final ShooterSubsystem shooter;
    private boolean usingCustomGoal = false;
    private AngularVelocity targetVelocity;

    @Inject
    public ShooterOutputCommand(ShooterSubsystem shooterSubsystem) {
        super(shooterSubsystem);
        this.shooter = shooterSubsystem;
        this.targetVelocity = RPM.of(shooterSubsystem.shootingTargetVelocity.get());
    }

    public void setTargetVelocity(AngularVelocity targetVelocity) {
        this.targetVelocity = targetVelocity;
        this.usingCustomGoal = true;
    }

    @Override
    public void initialize() {
        if (!this.usingCustomGoal) {
            this.targetVelocity = RPM.of(this.shooter.shootingTargetVelocity.get());
        }

        log.info("Shooting at {} RPM", this.targetVelocity.in(RPM));
        this.shooter.setTargetValue(this.targetVelocity);
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
