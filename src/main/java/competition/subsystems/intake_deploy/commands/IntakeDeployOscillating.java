package competition.subsystems.intake_deploy.commands;

import competition.Robot;
import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.Time;
import xbot.common.command.BaseSetpointCommand;
import xbot.common.controls.sensors.XTimer;
import xbot.common.properties.AngleProperty;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;


import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.Seconds;

public class IntakeDeployOscillating extends BaseSetpointCommand {

    public final IntakeDeploySubsystem intakeDeploySubsystem;
    public final AngleProperty amplitude;
    public final AngleProperty increasingValue;
    public final AngleProperty retractLimit;
    private Angle slowCloseTarget;
    public Angle currentTarget;
    public DoubleProperty period;
    public Time startTime;
    public double runOscillating;


    @Inject
    public IntakeDeployOscillating(IntakeDeploySubsystem intakeDeploySubsystem, PropertyFactory propertyFactory) {
        super(intakeDeploySubsystem);
        propertyFactory.setPrefix(this);
        this.intakeDeploySubsystem = intakeDeploySubsystem;
        this.amplitude = propertyFactory.createPersistentProperty("aptitude", Degrees.of(2));
        this.period = propertyFactory.createPersistentProperty("time per cycle", (1));
        this.retractLimit = propertyFactory.createPersistentProperty("RetractLimit", Degrees.of(-60));
        this.increasingValue = propertyFactory.createPersistentProperty("IncreasingValuePerSecond", Degrees.of(70));
    }

    @Override
    public void initialize() {
        super.initialize();
        startTime = XTimer.getFPGATimestampTime();
        slowCloseTarget = intakeDeploySubsystem.getCurrentValue();
    }
    public Angle oscillating() {
        Time commandDuration = Seconds.of(XTimer.getFPGATimestamp()).minus(startTime);
        runOscillating = amplitude.get().in(Degrees) * Math.sin(2 * Math.PI * commandDuration.in(Seconds) / period.get());
        return Degrees.of(runOscillating);
    }

    public Angle closePosition() {
        slowCloseTarget = slowCloseTarget.plus(increasingValue.get().times(Robot.LOOP_INTERVAL));

        if (slowCloseTarget.gt(retractLimit.get())) {
            slowCloseTarget = retractLimit.get();
        }
        return slowCloseTarget;
    }
    @Override
    public void execute() {
        currentTarget = closePosition().plus(oscillating());
        intakeDeploySubsystem.setTargetValue(currentTarget);
    }

    @Override
    public void end(boolean isInterrupted) {
        super.end(isInterrupted);
            this.intakeDeploySubsystem.setTargetValue(currentTarget);

    }
    @Override
    public boolean isFinished() {
        return false;
    }
}
