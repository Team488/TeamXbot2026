package competition.subsystems.intake_deploy.commands;

import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import edu.wpi.first.wpilibj.Timer;
import org.json.Property;
import xbot.common.command.BaseCommand;
import xbot.common.command.SimpleWaitForMaintainerCommand;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

public class IntakeDeployRetryCommand extends BaseCommand {
    IntakeDeploySubsystem intakeDeploySubsystem;
    public DoubleProperty timeout;
    public double startTime = 0;

    public IntakeDeployRetryCommand(IntakeDeploySubsystem intakeDeploy, PropertyFactory pf) {
        this.intakeDeploySubsystem = intakeDeploy;
        addRequirements(intakeDeploy);
        this.timeout = pf.createPersistentProperty("SecondsInTimeout", 3.0);
    }

    public boolean isTimeoutExpired() {
        //refer to WimpleWaitForMaintainerCommand
        return Timer.getFPGATimestamp() > startTime + timeout.get();
    }


    @Override
    public boolean isFinished() {
        return (isTimeoutExpired()) || intakeDeploySubsystem.intakeDeployIsExtended();
    }
}