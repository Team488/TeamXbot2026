package competition.subsystems.intake_deploy.commands;

import competition.Robot;
import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import edu.wpi.first.units.measure.Angle;
import xbot.common.command.BaseSetpointCommand;
import xbot.common.controls.sensors.XTimer;
import xbot.common.properties.AngleProperty;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.Degrees;

public class IntakeDeploySlowClosing extends BaseSetpointCommand {

    public final IntakeDeploySubsystem intakeDeploySubsystem;
    public final AngleProperty increasingValue;
    public final AngleProperty retractLimit;
    public final AngleProperty magnitude;
    public final DoubleProperty periodSeconds;

    private Angle centerTarget;
    private double startTime;

    @Inject
    public IntakeDeploySlowClosing(IntakeDeploySubsystem intakeDeploy,
                                   PropertyFactory propertyFactory) {
        super(intakeDeploy);
        propertyFactory.setPrefix(this);
        this.intakeDeploySubsystem = intakeDeploy;
        this.retractLimit = propertyFactory.createPersistentProperty("RetractLimit", Degrees.of(-30));
        this.increasingValue = propertyFactory.createPersistentProperty("IncreasingValuePerSecond", Degrees.of(70));
        this.magnitude = propertyFactory.createPersistentProperty("OscillationMagnitude", Degrees.of(5));
        this.periodSeconds = propertyFactory.createPersistentProperty("OscillationPeriodSeconds", 0.5);
    }

    @Override
    public void initialize() {
        super.initialize();
        // Start the running target from the mechanism's current position so the
        // first execute() call doesn't jump the setpoint.
        centerTarget = intakeDeploySubsystem.getCurrentValue();
        startTime = XTimer.getFPGATimestamp();
    }

    @Override
    public void execute() {
        var elapsed = XTimer.getFPGATimestamp() - startTime;
        // Increment our internal target each loop rather than reading the encoder,
        // so the setpoint moves smoothly regardless of where the mechanism actually is.
        // Convert degrees/second to degrees/loop using the known loop period.
        centerTarget = centerTarget.plus(increasingValue.get().times(Robot.LOOP_INTERVAL));

        // Don't let the center value go past the retract limit
        if (centerTarget.gt(retractLimit.get())) {
            centerTarget = retractLimit.get().copy();
        }

        Angle offsetDegrees = magnitude.get().times(Math.sin(2 * Math.PI * elapsed / periodSeconds.get()));

        // add oscillation on top of the steadily increasing target value
        var newTarget = centerTarget.plus(offsetDegrees);

        // clip value to valid ranges just in case
        if (newTarget.gt(Degrees.of(intakeDeploySubsystem.extendedPosition.get()))) {
            newTarget = Degrees.of(intakeDeploySubsystem.extendedPosition.get());
        } else if (newTarget.lt(Degrees.of(intakeDeploySubsystem.retractedPosition.get()))) {
            newTarget = Degrees.of(intakeDeploySubsystem.retractedPosition.get());
        }

        intakeDeploySubsystem.setTargetValue(newTarget);
    }

        @Override
        public boolean isFinished() {
            return false;
        }
}
