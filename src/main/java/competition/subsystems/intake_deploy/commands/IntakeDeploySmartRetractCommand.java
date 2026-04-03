package competition.subsystems.intake_deploy.commands;

import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import xbot.common.command.BaseSetpointCommand;
import xbot.common.command.SupportsSetpointLock;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

public class IntakeDeploySmartRetractCommand extends BaseSetpointCommand {

    @Inject
    public IntakeDeploySmartRetractCommand(IntakeDeploySubsystem intakeDeploy, PropertyFactory propertyFactory) {
        super(intakeDeploy);
    }
}
