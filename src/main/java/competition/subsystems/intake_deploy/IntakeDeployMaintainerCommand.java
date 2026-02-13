package competition.subsystems.intake_deploy;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Angle;
import xbot.common.command.BaseMaintainerCommand;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.logic.HumanVsMachineDecider;
import xbot.common.math.PIDManager;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.Rotations;

public class IntakeDeployMaintainerCommand extends BaseMaintainerCommand<Angle, Double> {
    private final IntakeDeploySubsystem subsystem;

    final PIDManager pidManager;

    @Inject
    public IntakeDeployMaintainerCommand(IntakeDeploySubsystem subsystemToMaintain, PropertyFactory pf,
                                         HumanVsMachineDecider.HumanVsMachineDeciderFactory hvmFactory,
                                         PIDManager.PIDManagerFactory pidManagerFactory) {
        super(subsystemToMaintain, pf, hvmFactory,0.01,0.01);
        pf.setPrefix(this);
        this.subsystem = subsystemToMaintain;
        this.pidManager = pidManagerFactory.create(
                pf.getPrefix() + "/IntakeDeployMaintainerPID",
                0,
                0,
                0,
                0,
                0);
    }

    @Override
    protected void coastAction() {
        this.subsystem.setPower(0.0);
    }

    @Override
    protected void calibratedMachineControlAction() {
        double power = pidManager.calculate(
                subsystem.getCurrentValue().in(Degrees),
                subsystem.getTargetValue().in(Degrees)
        );
        subsystem.setPower(power);
    }

    @Override
    protected double getErrorMagnitude() {
        return Math.abs(this.subsystem.getTargetValue().minus(this.subsystem.getCurrentValue()).in(Units.Degrees));
    }

    @Override
    protected Double getHumanInput() {
        return 0.0;
    }

    @Override
    protected double getHumanInputMagnitude() {
        return Math.abs(getHumanInput());
    }

    @Override
    public void initialize() {
        this.subsystem.setTargetValue(this.subsystem.getCurrentValue());
        this.subsystem.setPower(0.0);
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
        this.initialize();
    }
}