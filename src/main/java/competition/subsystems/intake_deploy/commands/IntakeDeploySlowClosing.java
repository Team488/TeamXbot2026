package competition.subsystems.intake_deploy.commands;

import competition.Robot;
import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import edu.wpi.first.units.measure.Angle;
import xbot.common.command.BaseSetpointCommand;
import xbot.common.properties.AngleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.Degrees;

public class IntakeDeploySlowClosing extends BaseSetpointCommand {

    public final IntakeDeploySubsystem intakeDeploySubsystem;
    public final AngleProperty increasingValue;
    public final AngleProperty retractLimit;
    private Angle currentTarget;

    @Inject
    public IntakeDeploySlowClosing(IntakeDeploySubsystem intakeDeploy,
                                   PropertyFactory propertyFactory) {
        super(intakeDeploy);
        propertyFactory.setPrefix(this);
        this.intakeDeploySubsystem = intakeDeploy;
        this.retractLimit = propertyFactory.createPersistentProperty("RetractLimit", Degrees.of(-30));
        this.increasingValue = propertyFactory.createPersistentProperty("IncreasingValuePerSecond", Degrees.of(70));
    }

    @Override
    public void initialize() {
        super.initialize();
        // Start the running target from the mechanism's current position so the
        // first execute() call doesn't jump the setpoint.
        currentTarget = intakeDeploySubsystem.getCurrentValue();
    }

    @Override
    public void execute() {
        // Increment our internal target each loop rather than reading the encoder,
        // so the setpoint moves smoothly regardless of where the mechanism actually is.
        // Convert degrees/second to degrees/loop using the known loop period.
        currentTarget = currentTarget.plus(increasingValue.get().times(Robot.LOOP_INTERVAL));

        if (currentTarget.gt(retractLimit.get())) {
            currentTarget = retractLimit.get().copy();
        }
        intakeDeploySubsystem.setTargetValue(currentTarget);
    }

        @Override
        public boolean isFinished() {
            return false;
        }
}
