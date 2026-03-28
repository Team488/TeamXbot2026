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
    public final AngleProperty aptitude;
    public final AngleProperty increasingValue;
    public final AngleProperty retractLimit;
    private Angle currentTarget;
    public DoubleProperty period;
    public Time startTime;
    public double runOscillating;
    public Angle targetvalue;

    @Inject
    public IntakeDeployOscillating(IntakeDeploySubsystem intakeDeploySubsystem, PropertyFactory propertyFactory) {
        super(intakeDeploySubsystem);
        propertyFactory.setPrefix(this);
        this.intakeDeploySubsystem = intakeDeploySubsystem;
        this.aptitude = propertyFactory.createPersistentProperty("aptitude", Degrees.of(20));
        this.period = propertyFactory.createPersistentProperty("Time of Oscillating once in second", (1));
        this.retractLimit = propertyFactory.createPersistentProperty("RetractLimit", Degrees.of(-30));
        this.increasingValue = propertyFactory.createPersistentProperty("IncreasingValuePerSecond", Degrees.of(70));
    }

    @Override
    public void initialize() {
        super.initialize();
        startTime = XTimer.getFPGATimestampTime();
        currentTarget = intakeDeploySubsystem.getCurrentValue();

    }

    public Angle oscillating() {
        Time commandDuration = startTime.minus(Seconds.of(XTimer.getFPGATimestamp()));
        intakeDeploySubsystem.setTargetValue(Degrees.of(runOscillating));
        runOscillating = aptitude.get().in(Degrees) * Math.sin(2 * Math.PI * commandDuration.in(Seconds) / period.get());
        return Degrees.of(runOscillating);
    }
    public void closePosition(Time time) {
        currentTarget = currentTarget.plus(increasingValue.get().times(Robot.LOOP_INTERVAL));
    }

    @Override
    public void execute() {
        if (currentTarget.gt(retractLimit.get())) {
            intakeDeploySubsystem.setTargetValue(retractLimit.get());
        } else {
            intakeDeploySubsystem.setTargetValue(currentTarget.plus(oscillating()));

        }

    }
    @Override
    public boolean isFinished() {
        return false;
    }
}
