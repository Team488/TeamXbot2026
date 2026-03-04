package competition.subsystems.climber.commands;

import competition.subsystems.climber.ClimberSubsystem;
import edu.wpi.first.units.measure.Angle;
import xbot.common.command.BaseMaintainerCommand;
import xbot.common.controls.sensors.XXboxController;
import xbot.common.logic.HumanVsMachineDecider;
import xbot.common.math.PIDManager;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.Degrees;

public class ClimberMaintainerCommand extends BaseMaintainerCommand<Angle, Double> {

    private final ClimberSubsystem climber;
    private PIDManager pidManager;
    private XXboxController manualControlGamepad;
    private double manualControlDeadband;

    @Inject
    public ClimberMaintainerCommand(ClimberSubsystem climber, PropertyFactory pf,
                                    HumanVsMachineDecider.HumanVsMachineDeciderFactory hvmFactory) {
        super(climber, pf, hvmFactory, 0.03, 0.1);
        this.climber = climber;
        addRequirements(climber);
    }

    @Override
    protected void coastAction() {
        climber.stop();
    }

    @Override
    protected void calibratedMachineControlAction() {
        Angle targetValue = climber.getTargetValue();
        climber.setPositionalGoalIncludingOffset(targetValue);
    }

    @Override
    protected double getErrorMagnitude() {
        Angle targetValue = climber.getTargetValue();
        Angle current = this.climber.getCurrentValue();
        Angle error = targetValue.minus(current);
        return error.in(Degrees);
    }

    @Override
    protected Double getHumanInput() {
//        var humanInput = MathUtil.applyDeadband(manualControlGamepad.getRightStickY(), manualControlDeadband);
//        aKitLog.setLogLevel(AKitLogger.LogLevel.DEBUG);
//        aKitLog.record("ManualControlInput", humanInput);
//        aKitLog.setLogLevel(AKitLogger.LogLevel.INFO);
//        return humanInput * this.climber.manualControlPower.get();
        // TODO: We need to configure a gamepad for human control.
        return 0.0;
    }

    @Override
    protected double getHumanInputMagnitude() {
        return getHumanInput();
    }

    @Override
    public void end(boolean interrupted) {
        if (interrupted) {
            climber.stop();
        }
    }
}
