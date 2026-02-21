package competition.subsystems.hood.commands;

import competition.subsystems.hood.HoodSubsystem;
import xbot.common.command.BaseMaintainerCommand;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.logic.HumanVsMachineDecider;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

public class HoodMaintainerCommand extends BaseMaintainerCommand<Double, Double> {

    final HoodSubsystem hood;

    @Inject
    public HoodMaintainerCommand(HoodSubsystem hood, PropertyFactory pf, HumanVsMachineDecider.HumanVsMachineDeciderFactory humanVsMachineDeciderFactory) {
        super(hood, pf, humanVsMachineDeciderFactory, .1, .1);
        this.hood = hood;
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    protected void coastAction() {

    }

    @Override
    protected void calibratedMachineControlAction() {
        var target = hood.getTargetValue();
        if (target != 0) {
            hood.runServo();
        }
    }

    @Override
    protected double getErrorMagnitude() {
        var target = hood.getTargetValue();
        var current = hood.getCurrentValue();
        return Math.abs(target - current);
    }

    @Override
    protected Double getHumanInput() {
        return 0.0;
    }

    @Override
    protected double getHumanInputMagnitude() {
        return Math.abs(getHumanInput());
    }
}
