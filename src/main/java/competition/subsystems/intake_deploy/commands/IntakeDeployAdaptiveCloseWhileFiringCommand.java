package competition.subsystems.intake_deploy.commands;

import competition.Robot;
import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import edu.wpi.first.units.measure.Angle;
import xbot.common.command.BaseSetpointCommand;
import xbot.common.controls.sensors.XTimer;
import xbot.common.logic.TimeStableValidator;
import xbot.common.properties.AngleProperty;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Degrees;

/**
 * Adaptively raises the intake to push balls toward the shooter. Advances upward until a current
 * spike is detected (indicating contact with balls), then pauses and oscillates to loosen them
 * before resuming. Repeats until reaching the retract limit.
 */
public class IntakeDeployAdaptiveCloseWhileFiringCommand extends BaseSetpointCommand {

    private enum State {
        ADVANCING,
        STALLED,
        AT_LIMIT_OSCILLATING
    }

    public final IntakeDeploySubsystem intakeDeploySubsystem;

    // Advance rate and limits
    public final AngleProperty advanceRatePerSecond;
    public final AngleProperty retractLimit;

    // Current spike detection
    public final DoubleProperty currentThresholdAmps;

    // Oscillation parameters (used while stalled)
    public final AngleProperty oscillationAmplitude;
    public final DoubleProperty oscillationPeriod;

    // How long current must stay above threshold to confirm a stall
    public final DoubleProperty currentStableTimeSeconds;

    // Dwell time after detecting a stall before resuming advance
    public final DoubleProperty dwellTimeSeconds;

    private final TimeStableValidator currentSpikeValidator;
    private State state;
    private Angle basePosition;
    private double stallStartTime;

    @Inject
    public IntakeDeployAdaptiveCloseWhileFiringCommand(IntakeDeploySubsystem intakeDeploySubsystem,
                                    PropertyFactory propertyFactory) {
        super(intakeDeploySubsystem);
        propertyFactory.setPrefix(this);
        this.intakeDeploySubsystem = intakeDeploySubsystem;

        this.advanceRatePerSecond = propertyFactory.createPersistentProperty(
                "AdvanceRatePerSecond", Degrees.of(25));
        this.retractLimit = propertyFactory.createPersistentProperty(
                "RetractLimit", Degrees.of(-90));
        this.currentThresholdAmps = propertyFactory.createPersistentProperty(
                "CurrentThresholdAmps", 15.0);
        this.oscillationAmplitude = propertyFactory.createPersistentProperty(
                "OscillationAmplitude", Degrees.of(10));
        this.oscillationPeriod = propertyFactory.createPersistentProperty(
                "OscillationPeriod", 0.5);
        this.currentStableTimeSeconds = propertyFactory.createPersistentProperty(
                "CurrentStableTimeSeconds", 0.25);
        this.dwellTimeSeconds = propertyFactory.createPersistentProperty(
                "DwellTimeSeconds", 0.75);

        this.currentSpikeValidator = new TimeStableValidator(() -> currentStableTimeSeconds.get());
    }

    @Override
    public void initialize() {
        super.initialize();
        basePosition = intakeDeploySubsystem.getCurrentValue();
        state = State.ADVANCING;
        stallStartTime = 0;
    }

    @Override
    public void execute() {
        double now = XTimer.getFPGATimestamp();
        double currentAmps = intakeDeploySubsystem.getMotorCurrent().in(Amps);
        boolean currentIsHigh = currentSpikeValidator.checkStable(currentAmps > currentThresholdAmps.get());

        switch (state) {
            case ADVANCING:
                // Move base position toward retract limit
                basePosition = basePosition.plus(advanceRatePerSecond.get().times(Robot.LOOP_INTERVAL));
                if (basePosition.gte(retractLimit.get())) {
                    basePosition = retractLimit.get();
                    state = State.AT_LIMIT_OSCILLATING;
                    stallStartTime = now;
                }

                // Transition to stalled only after current has been high for the stable window
                if (currentIsHigh && state == State.ADVANCING) {
                    state = State.STALLED;
                    stallStartTime = now;
                }

                intakeDeploySubsystem.setTargetValue(basePosition);
                break;

            case STALLED:
                // Oscillate around the stall position to loosen balls
                double elapsed = now - stallStartTime;
                double oscillation = oscillationAmplitude.get().in(Degrees)
                        * Math.sin(2 * Math.PI * elapsed / oscillationPeriod.get());
                intakeDeploySubsystem.setTargetValue(basePosition.plus(Degrees.of(oscillation)));

                // After dwell time, resume advancing
                if (elapsed >= dwellTimeSeconds.get()) {
                    state = State.ADVANCING;
                }
                break;

            case AT_LIMIT_OSCILLATING:
                // Reached the retract limit — just oscillate indefinitely
                double limitElapsed = now - stallStartTime;
                double limitOscillation = oscillationAmplitude.get().in(Degrees)
                        * Math.sin(2 * Math.PI * limitElapsed / oscillationPeriod.get());
                intakeDeploySubsystem.setTargetValue(retractLimit.get().plus(Degrees.of(limitOscillation)));
                break;

            default:
                break;
        }

        aKitLog.record("State", state.name());
        aKitLog.record("BasePosition", basePosition.in(Degrees));
        aKitLog.record("MotorCurrentAmps", currentAmps);
    }

    @Override
    public void end(boolean isInterrupted) {
        super.end(isInterrupted);
        intakeDeploySubsystem.setTargetValue(basePosition);
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
