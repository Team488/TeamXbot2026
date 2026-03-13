package competition.subsystems.shooter.commands;

import competition.subsystems.shooter.ShooterSubsystem;
import edu.wpi.first.units.measure.AngularVelocity;
import xbot.common.command.BaseSetpointCommand;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.RPM;

import java.util.function.Supplier;

public class ShooterOutputCommand extends BaseSetpointCommand {
    private final ShooterSubsystem shooter;
    private Supplier<AngularVelocity> targetVelocitySupplier = null;

    @Inject
    public ShooterOutputCommand(ShooterSubsystem shooterSubsystem) {
        super(shooterSubsystem);
        this.shooter = shooterSubsystem;
        this.targetVelocitySupplier = () -> RPM.of(shooterSubsystem.defaultShootingVelocity.get());
    }

    public void setTargetVelocity(AngularVelocity targetVelocity) {
        this.targetVelocitySupplier = () -> targetVelocity;
    }

    public void setTargetVelocity(Supplier<AngularVelocity> targetVelocitySupplier) {
        this.targetVelocitySupplier = targetVelocitySupplier;
    }

    @Override
    public void initialize() {
        var targetVelocity = targetVelocitySupplier.get();

        log.info("Shooting at {} RPM", targetVelocity.in(RPM));
        this.shooter.setTargetValue(targetVelocity);
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
