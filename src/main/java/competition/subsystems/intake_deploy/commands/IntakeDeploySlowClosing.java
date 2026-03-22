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

    private Angle currentTarget;
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
        currentTarget = intakeDeploySubsystem.getCurrentValue();
        startTime = XTimer.getFPGATimestamp()
    }

    @Override
    public void execute() {
        var elapsed = XTimer.getFPGATimestamp() - startTime;
        // Increment our internal target each loop rather than reading the encoder,
        // so the setpoint moves smoothly regardless of where the mechanism actually is.
        // Convert degrees/second to degrees/loop using the known loop period.
        currentTarget = currentTarget.plus(increasingValue.get().times(Robot.LOOP_INTERVAL));

        if (currentTarget.gt(retractLimit.get())) {
            currentTarget = retractLimit.get().copy();
        }

        Angle offsetDegrees = magnitude.get().times(Math.sin(2 * Math.PI * elapsed / periodSeconds.get()));

        // add oscillation on top of the steadily increasing target value
        intakeDeploySubsystem.setTargetValue(currentTarget.plus(offsetDegrees));
    }

        @Override
        public boolean isFinished() {
            return false;
        }
}
