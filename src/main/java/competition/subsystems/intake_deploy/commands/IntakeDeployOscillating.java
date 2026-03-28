package competition.subsystems.intake_deploy.commands;

import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import edu.wpi.first.units.measure.Time;
import xbot.common.command.BaseSetpointCommand;
import xbot.common.controls.sensors.XTimer;
import xbot.common.properties.AngleProperty;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;


import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.Second;
import static edu.wpi.first.units.Units.Seconds;

public class IntakeDeployOscillating extends BaseSetpointCommand {

    public IntakeDeploySubsystem intake;
    public final AngleProperty aptitude;
    public DoubleProperty period;
    public Time startTime;
    public double getCurrentTime;
    public Time commandRunCurrentTime;
    public double runOscillating;

    @Inject
    public IntakeDeployOscillating(IntakeDeploySubsystem intakeDeploySubsystem, PropertyFactory propertyFactory) {
        super(intakeDeploySubsystem);
        propertyFactory.setPrefix(this);
        this.aptitude = propertyFactory.createPersistentProperty("aptitude", Degrees.of(10));
        this.period = propertyFactory.createPersistentProperty("Time of Oscillating once in second", (1));
    }

    @Override
    public void initialize() {
    startTime = XTimer.getFPGATimestampTime();
    getCurrentTime = XTimer.getFPGATimestamp();
    }
    @Override
    //Aptitude * sin(2*PI*Time/period)
    public void execute() {
        commandRunCurrentTime = startTime.minus(Second.of(getCurrentTime));
        runOscillating = aptitude.get().in(Degrees) * Math.sin(2 * Math.PI * commandRunCurrentTime.in(Seconds) / period.get());

    }
    @Override
    public boolean isFinished() {
        return false;
    }
}
