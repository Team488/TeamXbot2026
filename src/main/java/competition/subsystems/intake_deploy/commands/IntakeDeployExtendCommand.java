package competition.subsystems.intake_deploy.commands;

import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import xbot.common.command.BaseSetpointCommand;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.controls.sensors.XTimer;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.Degrees;

public class IntakeDeployExtendCommand extends BaseSetpointCommand {
    final IntakeDeploySubsystem intakeDeploy;

    @Inject
    public IntakeDeployExtendCommand(IntakeDeploySubsystem intakeDeploy) {
        super(intakeDeploy);
        this.intakeDeploy = intakeDeploy;
    }

    @Override
    public void initialize() {
        super.initialize();
        intakeDeploy.setTargetValue(Degrees.of(intakeDeploy.extendedPosition.get()));
        log.info("Initialized IntakeDeployExtend");
    }
}