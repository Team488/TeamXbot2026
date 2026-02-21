package competition.subsystems.climber.commands;

import competition.subsystems.climber.ClimberSubsystem;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.units.measure.Angle;
import xbot.common.advantage.AKitLogger;
import xbot.common.command.BaseMaintainerCommand;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.controls.sensors.XXboxController;
import xbot.common.logic.HumanVsMachineDecider;
import xbot.common.math.MathUtils;
import xbot.common.math.PIDManager;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.Rotations;

public class ClimberMaintainer extends BaseMaintainerCommand <Angle, Double> {

    private ClimberSubsystem climber;
    private PIDManager pidManager;
    private XXboxController manualContrlGamepad;
    private double manualControlDeadband;

    @Inject
    public ClimberMaintainer(ClimberSubsystem climber,
                             HumanVsMachineDecider.HumanVsMachineDeciderFactory hvmFactory,
                             PropertyFactory pf, PIDManager.PIDManagerFactory pidManagerFactory) {
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
        Angle current = this.climber.getCurrentValue();
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
        var humanInput = MathUtil.applyDeadband(manualContrlGamepad.getRightStickY(), manualControlDeadband);
        aKitLog.setLogLevel(AKitLogger.LogLevel.DEBUG);
        aKitLog.record("ManualControlInput", humanInput);
        aKitLog.setLogLevel(AKitLogger.LogLevel.INFO);
        return humanInput * this.climber.manualControlPower.get();
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
