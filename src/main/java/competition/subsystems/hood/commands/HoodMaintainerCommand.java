package competition.subsystems.hood.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.hood.HoodSubsystem;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj.DriverStation;
import xbot.common.command.BaseMaintainerCommand;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.logic.HumanVsMachineDecider;
import xbot.common.math.MathUtils;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

public class HoodMaintainerCommand extends BaseMaintainerCommand<Double, Double> {

    final HoodSubsystem hood;
    final OperatorInterface oi;

    @Inject
    public HoodMaintainerCommand(HoodSubsystem hood,
                                 PropertyFactory pf,
                                 HumanVsMachineDecider.HumanVsMachineDeciderFactory humanVsMachineDeciderFactory,
                                 OperatorInterface operatorInterface
    ) {
        super(hood, pf, humanVsMachineDeciderFactory, .1, .1);
        this.hood = hood;
        this.oi = operatorInterface;
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    protected void coastAction() {

    }

    @Override
    protected void initializeMachineControlAction() {
        // The base implementation here will reset the target (probably incorrectly). Fixing it here
        // to avoid side-effects while we reason through a better implementation of the base class.
        if (this.hood.getSetpointLock().getCurrentCommand() != null && !DriverStation.isAutonomous()) {
            this.hood.getSetpointLock().getCurrentCommand().cancel();
        }
    }

    @Override
    protected void calibratedMachineControlAction() {
        var target = hood.getTargetValue();
        if (target != null) {
            hood.runServo();
        }
    }

    @Override
    protected double getErrorMagnitude() {
        var target = hood.getTargetValue();
        var current = hood.getCurrentValue();
        return Math.abs(target - current);
    }

    public void setTargetRatio(double ratio) {
        hood.setTargetValue(ratio);
    }

    @Override
    protected Double getHumanInput() {
//        Uncommenting this because this is just for testing manually, uncomment if testing is needed.
//        var humanInput = MathUtil.applyDeadband(oi.setupDebugGamepad.getLeftStickY(), oi.getOperatorGamepadTypicalDeadband());
//        humanInput = MathUtils.constrainDouble(humanInput, 0, 0.65);
//        hood.setTargetValue(humanInput);
        return 0.0;
    }
    @Override
    protected double getHumanInputMagnitude() {
        return Math.abs(getHumanInput());
    }
}
