package competition.subsystems.climber.commands;

import competition.subsystems.climber.ClimberSubsystem;
import edu.wpi.first.units.measure.Angle;
import xbot.common.command.BaseMaintainerCommand;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.logic.HumanVsMachineDecider;
import xbot.common.math.PIDManager;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.Rotations;

public class ClimberMaintainer extends BaseMaintainerCommand <Angle, Double> {

    public ClimberSubsystem climber;
    public DoubleProperty extendPower;
    public DoubleProperty retractPower;
    public XCANMotorController climberMotor;

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
        Angle target = climber.getTargetValue();
        if ((target.in(Rotations) >= 0.02)) {
            climber.setPower(0.0);
        } else {
            climber.setPower(0.02);
        }
    }

    @Override
    protected double getErrorMagnitude() {
        Angle current = climber.getCurrentValue();
        Angle target = climber.getTargetValue();
        Angle error = target.minus(current);

        return error.in(Rotations);
    }

    @Override
    protected Double getHumanInput() {
        return 0.0; //No human input
    }

    @Override
    protected double getHumanInputMagnitude() {
        return 0; //No human input
    }

    @Override
    public void end(boolean interrupted) {
        if (interrupted) {
            climber.stop();
        }
    }
}
