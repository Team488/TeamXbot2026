package competition.subsystems.intake_deploy.commands;

import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import xbot.common.command.BaseCommand;
import xbot.common.logic.TimeStableValidator;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.Amps;

/**
 * Command that finishes when either the intake end stop is hit or the current limit is exceeded for a time.
 */
public class IntakeDeployWaitForEndStopOrCurrentLimitCommand extends BaseCommand {
    private final IntakeDeploySubsystem subsystem;

    private final TimeStableValidator currentLimitTimer;
    private final DoubleProperty currentLimitTimeoutSeconds;
    private final DoubleProperty endStopCurrentLimit;

    @Inject
    public IntakeDeployWaitForEndStopOrCurrentLimitCommand(IntakeDeploySubsystem subsystem, PropertyFactory pf) {
        this.subsystem = subsystem;

        pf.setPrefix(this);
        this.currentLimitTimeoutSeconds = pf.createPersistentProperty("CurrentLimitTimeoutSeconds", 0.2);
        this.endStopCurrentLimit = pf.createPersistentProperty("EndStopCurrentLimitAmps", 15.0);

        this.currentLimitTimer = new TimeStableValidator(this.currentLimitTimeoutSeconds::get);
    }

    @Override
    public void execute() {
        // Feed in current to the current limit validator
        this.currentLimitTimer.checkStable(subsystem.isTouchingIntakeDeploy());
    }

    @Override
    public boolean isFinished() {
        return (
                this.subsystem.isTouchingIntakeDeployExtendedSensor()
                        || (isExceedingCurrentLimit() && this.currentLimitTimer.peekStable()));
    }

    private boolean isExceedingCurrentLimit() {
        return this.subsystem.getMotorCurrent().in(Amps) >= this.endStopCurrentLimit.get();
    }
}
