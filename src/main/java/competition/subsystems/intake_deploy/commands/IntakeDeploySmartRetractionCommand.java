package competition.subsystems.intake_deploy.commands;

import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import xbot.common.command.BaseSetpointCommand;
import xbot.common.controls.sensors.XTimer;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Degrees;

public class IntakeDeploySmartRetractionCommand extends BaseSetpointCommand {
    private final IntakeDeploySubsystem intakeDeploy;
    private final DoubleProperty currentUpperThreshold;
    private final DoubleProperty currentLowerThreshold;
    private final DoubleProperty timeWaited;

    private double stopTime = 0;

    @Inject
    public IntakeDeploySmartRetractionCommand(IntakeDeploySubsystem intakeDeploy, PropertyFactory propertyFactory) {
        super(intakeDeploy);
        this.intakeDeploy = intakeDeploy;
        propertyFactory.setPrefix(this);
        this.currentUpperThreshold = propertyFactory.createPersistentProperty("Current Upper Threshold", 6);
        this.currentLowerThreshold = propertyFactory.createPersistentProperty("Current Lower Threshold", 4);
        this.timeWaited = propertyFactory.createPersistentProperty("Time Before Reattempt", 0.5);
    }

    @Override
    public void initialize() {
        log.info("Initialized IntakeDeploySmartRetraction");
    }

    @Override
    public void execute() {
        double elapsedTimeFromStop = XTimer.getFPGATimestamp() - stopTime;
        var currentIsAboveLowerThreshold = intakeDeploy.intakeDeployMotor.getCurrent().gt(Amps.of(currentLowerThreshold.get()));
        var currentIsBelowUpperThreshold = intakeDeploy.intakeDeployMotor.getCurrent().lt(Amps.of(currentUpperThreshold.get()));

        if (currentIsAboveLowerThreshold && currentIsBelowUpperThreshold) {
            intakeDeploy.setTargetValue(intakeDeploy.getCurrentValue()); //stops the intake
            stopTime = XTimer.getFPGATimestamp();
        }

        if (elapsedTimeFromStop >= timeWaited.get()) { // have 500 milliseconds passed since stopping intake
            stopTime = 0;
            intakeDeploy.setTargetValue(Degrees.of(intakeDeploy.retractedPosition.get()));
        }
    }

    @Override
    public boolean isFinished() { return false; }

    @Override
    public void end(boolean interrupted) {
        intakeDeploy.setTargetValue(Degrees.of(intakeDeploy.extendedPosition.get()));
    }

}
