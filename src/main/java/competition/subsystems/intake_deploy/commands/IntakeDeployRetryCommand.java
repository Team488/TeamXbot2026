package competition.subsystems.intake_deploy.commands;

import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import edu.wpi.first.wpilibj.Timer;
import xbot.common.command.BaseCommand;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

public class IntakeDeployRetryCommand extends BaseCommand {
    IntakeDeploySubsystem intakeDeploySubsystem;
    public DoubleProperty timeout;
    public double startTime = 0;
    public boolean extended = false;

    public IntakeDeployRetryCommand(IntakeDeploySubsystem intakeDeploy, PropertyFactory pf) {
        this.intakeDeploySubsystem = intakeDeploy;
        addRequirements(intakeDeploy);
        this.timeout = pf.createPersistentProperty("SecondsInTimeout", 3.0);
    }

    public void initialize() {
        startTime = Timer.getFPGATimestamp();
        intakeDeploySubsystem.setTargetValue(intakeDeploySubsystem.extendedPosition); //position we want to get to
    }

    public boolean isTimeoutExpired() {
        return Timer.getFPGATimestamp() > startTime + timeout.get();
    }

    @Override
    public void execute() {
        //if the intake deploy is extent
        if (!intakeDeploySubsystem.intakeDeployIsExtended() && isTimeoutExpired()) {
            intakeDeploySubsystem.setTargetValue(intakeDeploySubsystem.retractedPosition);
            Timer.delay(0.2); //delay for testing only unless wanted in comp
            intakeDeploySubsystem.setTargetValue(intakeDeploySubsystem.extendedPosition);
            startTime = Timer.getFPGATimestamp();
        }
        return;
    }

    @Override
    public boolean isFinished() {
        return (isTimeoutExpired()) || intakeDeploySubsystem.intakeDeployIsExtended();

    }

    //if timeout.get() is over 3 seconds, retract intake deploy.
}