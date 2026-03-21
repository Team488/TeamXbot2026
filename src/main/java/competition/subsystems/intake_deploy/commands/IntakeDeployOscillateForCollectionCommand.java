package competition.subsystems.intake_deploy.commands;

import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import xbot.common.command.BaseSetpointCommand;
import xbot.common.controls.sensors.XTimer;
import xbot.common.properties.AngleProperty;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.Degrees;

public class IntakeDeployOscillateForCollectionCommand extends BaseSetpointCommand {

    final IntakeDeploySubsystem intakeDeploy;
    final AngleProperty oscillationMagnitude;
    final DoubleProperty oscillationPeriod;
    final DoubleProperty dwellFraction;
    double startTime;

    @Inject
    public IntakeDeployOscillateForCollectionCommand(IntakeDeploySubsystem intakeDeploy, PropertyFactory propertyFactory) {
        super(intakeDeploy);
        this.intakeDeploy = intakeDeploy;
        propertyFactory.setPrefix(this);
        this.oscillationMagnitude = propertyFactory.createPersistentProperty("OscillationMagnitude", Degrees.of(2.0));
        this.oscillationPeriod = propertyFactory.createPersistentProperty("OscillationPeriodSeconds", 1.0);
        this.dwellFraction = propertyFactory.createPersistentProperty("DwellFraction", 0.5);
    }

    @Override
    public void initialize() {
        log.info("Initialized IntakeDeployOscillateForCollection");
        // Record when the command started so we can measure elapsed time in execute().
        startTime = XTimer.getFPGATimestamp();
    }

    @Override
    public void execute() {
        // The intake oscillates around this command's own base position.
        double baseDegrees = intakeDeploy.extendedPosition.get();
        double magnitude = oscillationMagnitude.get().in(Degrees);
        double period = oscillationPeriod.get();
        double elapsed = XTimer.getFPGATimestamp() - startTime;

        // The cycle is split into two phases:
        //   Dwell phase  (0 to dwellFraction * period): intake stays at basePosition (offset = 0)
        //   Lift phase   (remainder of period): intake follows a smooth cosine pulse up and back.
        //
        // The lift shape is (1 - cos(2π*t)) / 2, which:
        //   - starts and ends at 0 with zero velocity (no jerk at the dwell boundary)
        //   - peaks at `magnitude` at the midpoint of the lift phase
        //
        // Example: magnitude=5, period=1, dwellFraction=0.5
        //   -> intake sits at base for 0.5 s, then smoothly lifts 5° and returns over the next 0.5 s.
        double phase = elapsed % period;
        double dwellDuration = dwellFraction.get() * period;
        double liftDuration = period - dwellDuration;

        double offsetDegrees;
        if (phase < dwellDuration || liftDuration <= 0) {
            offsetDegrees = 0;
        } else {
            double liftPhase = (phase - dwellDuration) / liftDuration; // normalized 0→1
            offsetDegrees = magnitude * (1 - Math.cos(2 * Math.PI * liftPhase)) / 2.0;
        }
        intakeDeploy.setTargetValue(Degrees.of(baseDegrees + offsetDegrees));
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public void end(boolean interrupted) {
        // Stop the oscillation and hold the intake at the extended position.
        intakeDeploy.setTargetValue(Degrees.of(intakeDeploy.extendedPosition.get()));
    }
}
