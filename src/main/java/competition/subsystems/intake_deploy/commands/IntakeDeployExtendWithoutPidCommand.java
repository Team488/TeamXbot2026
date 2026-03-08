package competition.subsystems.intake_deploy.commands;

import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import xbot.common.command.BaseCommand;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import java.util.function.DoubleSupplier;

/**
 * Extend the intake deploy mechanism without relying on PID. Used for driving the intake down towards the end stop.
 */
public class IntakeDeployExtendWithoutPidCommand extends BaseCommand {
    private final IntakeDeploySubsystem subsystem;

    private final DoubleProperty powerProperty;
    private DoubleSupplier overrideSupplier = null;

    @Inject
    public IntakeDeployExtendWithoutPidCommand(IntakeDeploySubsystem subsystem, PropertyFactory pf) {
        addRequirements(subsystem);
        this.subsystem = subsystem;

        pf.setPrefix(this);
        this.powerProperty = pf.createPersistentProperty("Power", -0.2);
    }

    public IntakeDeployExtendWithoutPidCommand setPower(DoubleSupplier supplier) {
        this.overrideSupplier = supplier;
        return this;
    }

    public void initialize() {
        if (this.overrideSupplier != null) {
            this.subsystem.setPower(this.overrideSupplier.getAsDouble());
        } else {
            this.subsystem.setPower(this.powerProperty.get());
        }
    }

    @Override
    public void end(boolean isInterrupted) {
        this.subsystem.stop();
    }
}
