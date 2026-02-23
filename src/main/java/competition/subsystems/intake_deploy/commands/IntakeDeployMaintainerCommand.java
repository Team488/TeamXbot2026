package competition.subsystems.intake_deploy.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Angle;
import xbot.common.advantage.AKitLogger;
import xbot.common.command.BaseMaintainerCommand;
import xbot.common.controls.sensors.XXboxController;
import xbot.common.logic.HumanVsMachineDecider;
import xbot.common.math.MathUtils;
import xbot.common.math.PIDManager;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

public class IntakeDeployMaintainerCommand extends BaseMaintainerCommand<Angle, Double> {
    private final IntakeDeploySubsystem subsystem;
    private final XXboxController manualControlGamepad;
    private final double manualControlDeadband;

    @Inject
    public IntakeDeployMaintainerCommand(IntakeDeploySubsystem subsystemToMaintain, PropertyFactory pf,
                                         OperatorInterface oi,
                                         HumanVsMachineDecider.HumanVsMachineDeciderFactory hvmFactory,
                                         PIDManager.PIDManagerFactory pidManagerFactory) {
        super(subsystemToMaintain, pf, hvmFactory,0.01,0.01);
        pf.setPrefix(this);
        this.subsystem = subsystemToMaintain;
        this.manualControlGamepad = oi.setupDebugGamepad;
        this.manualControlDeadband = oi.getOperatorGamepadTypicalDeadband();
    }

    @Override
    protected void coastAction() {
        this.subsystem.setPower(0.0);
    }

    @Override
    protected void calibratedMachineControlAction() {
        subsystem.setPositionGoal(subsystem.getTargetValue());
    }

    @Override
    protected double getErrorMagnitude() {
        return Math.abs(this.subsystem.getTargetValue().minus(this.subsystem.getCurrentValue()).in(Units.Degrees));
    }

    @Override
    protected Double getHumanInput() {
        var humanInput = MathUtil.applyDeadband(manualControlGamepad.getLeftStickY(), manualControlDeadband);
        aKitLog.setLogLevel(AKitLogger.LogLevel.DEBUG);
        aKitLog.record("ManualControlInput", humanInput);
        aKitLog.setLogLevel(AKitLogger.LogLevel.INFO);
        return humanInput * this.subsystem.manualControlPower.get();
    }

    @Override
    protected double getHumanInputMagnitude() {
        return getHumanInput();
    }

    @Override
    public void initialize() {
        this.subsystem.setPower(0.0);
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
        this.initialize();
    }
}