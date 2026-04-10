package competition.subsystems.shooter.commands;

import competition.subsystems.shooter.ShooterSubsystem;
import xbot.common.command.BaseSetpointCommand;
import xbot.common.properties.AngularVelocityProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.RPM;

public class ShooterDefaultCommand extends BaseSetpointCommand {
    private final ShooterSubsystem shooter;
    private final AngularVelocityProperty warmUpVelocity;

    @Inject
    public ShooterDefaultCommand(ShooterSubsystem shooterSubsystem, PropertyFactory pf) {
        super(shooterSubsystem);
        this.shooter = shooterSubsystem;
        pf.setPrefix(this);
        this.warmUpVelocity = pf.createPersistentProperty("WarmUpVelocity", RPM.of(2500));
    }

    @Override
    public void initialize() {
        super.initialize();
        this.shooter.setTargetValue(warmUpVelocity.get());
    }

    @Override
    public boolean runsWhenDisabled() {
        return true;
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
