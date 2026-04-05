package competition.subsystems.intake_deploy.commands;

import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import edu.wpi.first.units.measure.Angle;
import xbot.common.command.BaseSetpointCommand;
import xbot.common.controls.sensors.XTimer;
import xbot.common.properties.AngleProperty;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Degrees;

public class IntakeDeploySmartRetractionCommand extends BaseSetpointCommand {
    private final IntakeDeploySubsystem intakeDeploy;
    private final DoubleProperty currentThreshold;
    private final DoubleProperty oscillationDelayTime;

    public final AngleProperty amplitude;
    
    public DoubleProperty period;

    private double stopTime = 0;
    private boolean isReadyToOscillate = false;
    private Angle oscillationTarget;
    private double oscillationTime;

    @Inject
    public IntakeDeploySmartRetractionCommand(IntakeDeploySubsystem intakeDeploy, PropertyFactory propertyFactory) {
        super(intakeDeploy);
        this.intakeDeploy = intakeDeploy;
        propertyFactory.setPrefix(this);

        this.currentThreshold = propertyFactory.createPersistentProperty("Current Threshold", 45);
        this.oscillationDelayTime = propertyFactory.createPersistentProperty("Time Before Oscillation", 0.5);
        this.amplitude = propertyFactory.createPersistentProperty("Amplitude", Degrees.of(7.5));
        this.period = propertyFactory.createPersistentProperty("Time per cycle", 1);
    }

    @Override
    public void initialize() {
        log.info("Initialized IntakeDeploySmartRetraction");
        intakeDeploy.setTargetValue(Degrees.of(intakeDeploy.retractedPosition.get()));
    }

    public Angle oscillate(double time) {
        double runOscillation = amplitude.get().in(Degrees) * Math.sin(2 * Math.PI * time / period.get());
        return Degrees.of(runOscillation);
    }

    @Override
    public void execute() {
        var currentIsAboveThreshold = intakeDeploy.intakeDeployMotor.getCurrent().gt(Amps.of(currentThreshold.get()));

        if (currentIsAboveThreshold && !isReadyToOscillate && stopTime == 0) {
            intakeDeploy.setTargetValue(intakeDeploy.getCurrentValue()); // stops the intake
            stopTime = XTimer.getFPGATimestamp();
        }

        if (stopTime > 0 && !isReadyToOscillate) {
            double elapsedTimeFromStop = XTimer.getFPGATimestamp() - stopTime;

            if (elapsedTimeFromStop >= oscillationDelayTime.get()) { // have 500 milliseconds passed since stopping intake
                stopTime = 0;
                isReadyToOscillate = true;
                oscillationTarget = intakeDeploy.getCurrentValue();
                oscillationTime = XTimer.getFPGATimestamp();
            }
        }
        
        if (isReadyToOscillate) {
            double timeSinceOscillation = XTimer.getFPGATimestamp() - oscillationTime;
            Angle target = oscillationTarget.plus(oscillate(timeSinceOscillation));
            intakeDeploy.setTargetValue(target);
        }
    }

    @Override
    public boolean isFinished() { return false; }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
        intakeDeploy.setTargetValue(Degrees.of(intakeDeploy.extendedPosition.get()));
    }

}
