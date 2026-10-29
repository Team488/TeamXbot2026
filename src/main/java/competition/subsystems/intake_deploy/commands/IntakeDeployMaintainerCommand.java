package competition.subsystems.intake_deploy.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.wpilibj.DriverStation;
import xbot.common.advantage.AKitLogger;
import xbot.common.command.BaseMaintainerCommand;
import xbot.common.controls.sensors.XXboxController;
import xbot.common.logic.HumanVsMachineDecider;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

public class IntakeDeployMaintainerCommand extends BaseMaintainerCommand<Angle, Double> {
    private final IntakeDeploySubsystem subsystem;
    private final XXboxController manualControlGamepad;
    private final double manualControlDeadband;

    @Inject
    public IntakeDeployMaintainerCommand(IntakeDeploySubsystem subsystemToMaintain, PropertyFactory pf,
                                         OperatorInterface oi,
                                         HumanVsMachineDecider.HumanVsMachineDeciderFactory hvmFactory) {
        super(subsystemToMaintain, pf, hvmFactory,5.0,0.05);
        pf.setPrefix(this);
        this.subsystem = subsystemToMaintain;
        this.manualControlGamepad = oi.operatorGamepad;
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
        aKitLog.withLogLevel(AKitLogger.LogLevel.DEBUG, () -> {
            aKitLog.record("ManualControlInput", humanInput);
        });
        return humanInput;
    }

    @Override
    protected double getHumanInputMagnitude() {
        return getHumanInput();
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    protected void initializeMachineControlAction() {
        // The base implementation here will reset the target (probably incorrectly). Fixing it here
        // to avoid side-effects while we reason through a better implementation of the base class.
        if (this.subsystem.getSetpointLock().getCurrentCommand() != null && !DriverStation.isAutonomous()) {
            this.subsystem.getSetpointLock().getCurrentCommand().cancel();
        }

        // only do this if we're not in autonomous, otherwise it can replace a goal set by auto right at the start
        if(!DriverStation.isAutonomous()) {
            // Typically set the goal to the current position, to avoid sudden extreme
            // changes as soon as Coast is complete.
            this.subsystem.setTargetValue(this.subsystem.getCurrentValue());
        }
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
        this.initialize();
    }
}