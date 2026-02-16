package competition.subsystems.hood.commands;

import xbot.common.command.BaseMaintainerCommand;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.logic.HumanVsMachineDecider;
import xbot.common.properties.PropertyFactory;

public class HoodMaintainerCommand extends BaseMaintainerCommand<Double, Double> {
    public HoodMaintainerCommand(BaseSetpointSubsystem<Double, Double> subsystemToMaintain, PropertyFactory pf, HumanVsMachineDecider.HumanVsMachineDeciderFactory humanVsMachineDeciderFactory, double defaultErrorTolerance, double defaultTimeStableWindow) {
        super(subsystemToMaintain, pf, humanVsMachineDeciderFactory, defaultErrorTolerance, defaultTimeStableWindow);
    }

    @Override
    protected void coastAction() {

    }

    @Override
    protected void calibratedMachineControlAction() {

    }

    @Override
    protected double getErrorMagnitude() {
        return 0;
    }

    @Override
    protected Double getHumanInput() {
        return 0.0;
    }

    @Override
    protected double getHumanInputMagnitude() {
        return 0;
    }
}
