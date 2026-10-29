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
    final AngleProperty basePosition;
    final AngleProperty oscillationMagnitude;
    final DoubleProperty oscillationPeriod;
    double startTime;

    @Inject
    public IntakeDeployOscillateForCollectionCommand(IntakeDeploySubsystem intakeDeploy, PropertyFactory propertyFactory) {
        super(intakeDeploy);
        this.intakeDeploy = intakeDeploy;
        propertyFactory.setPrefix(this);
        this.basePosition = propertyFactory.createPersistentProperty("BasePosition", Degrees.of(-134.0));
        this.oscillationMagnitude = propertyFactory.createPersistentProperty("OscillationMagnitude", Degrees.of(2.0));
        this.oscillationPeriod = propertyFactory.createPersistentProperty("OscillationPeriodSeconds", 1.0);
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
        double baseDegrees = basePosition.get().in(Degrees);
        double magnitude = oscillationMagnitude.get().in(Degrees);
        double period = oscillationPeriod.get();
        double elapsed = XTimer.getFPGATimestamp() - startTime;

        // A sine wave completes one full cycle every `period` seconds.
        // Multiplying by `magnitude` scales the wave so the intake moves that many
        // degrees above and below the base position.
        // Example: magnitude=5, period=1 -> intake sweeps from -140 to -130 degrees once per second.
        double offsetDegrees = magnitude * Math.sin(2 * Math.PI * elapsed / period);
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
