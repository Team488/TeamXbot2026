package competition.subsystems.intake_deploy.commands;

import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import xbot.common.command.BaseSetpointCommand;
import xbot.common.controls.sensors.XTimer;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.Degrees;

public class IntakeDeployRetryCommand extends BaseSetpointCommand {

    final IntakeDeploySubsystem intakeDeploy;
    public double preTime;
    public double timePassed;

    @Inject
    public IntakeDeployRetryCommand(IntakeDeploySubsystem intakeDeploy) {
        super(intakeDeploy);
        this.intakeDeploy = intakeDeploy;
    }

    @Override
    public void initialize() {
        super.initialize();
        preTime = XTimer.getFPGATimestamp();
    }

    @Override
    public void execute() {
        super.execute();
        timePassed = XTimer.getFPGATimestamp() - preTime;
        if (timePassed >= 5 && intakeDeploy.getCurrentValue() != intakeDeploy.getTargetValue()) {
            intakeDeploy.setTargetValue(Degrees.of(intakeDeploy.retractedPosition.get()));
        }
    }
}