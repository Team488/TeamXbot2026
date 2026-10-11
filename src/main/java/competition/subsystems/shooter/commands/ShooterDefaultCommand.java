package competition.subsystems.shooter.commands;

import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.shooter.ShooterSubsystem;
import xbot.common.command.BaseSetpointCommand;
import xbot.common.properties.AngularVelocityProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.RPM;

public class ShooterDefaultCommand extends BaseSetpointCommand {
    private final ShooterSubsystem shooter;
    private final PoseSubsystem pose;
    private final AngularVelocityProperty warmUpVelocity;

    @Inject
    public ShooterDefaultCommand(ShooterSubsystem shooterSubsystem, PoseSubsystem pose, PropertyFactory pf) {
        super(shooterSubsystem);
        this.shooter = shooterSubsystem;
        this.pose = pose;
        pf.setPrefix(this);
        this.warmUpVelocity = pf.createPersistentProperty("WarmUpVelocity", RPM.of(2500));
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void execute() {
        if (pose.isInAllianceZone()) {
            this.shooter.setTargetValue(warmUpVelocity.get());
        } else {
            this.shooter.setTargetValue(RPM.of(0));
        }
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
