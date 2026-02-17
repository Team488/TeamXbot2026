package competition.subsystems.climber.commands;

import competition.subsystems.climber.ClimberSubsystem;
import edu.wpi.first.units.measure.Angle;
import xbot.common.command.BaseMaintainerCommand;
import xbot.common.logic.HumanVsMachineDecider;
import xbot.common.math.PIDManager;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

public class ClimberMaintainer extends BaseMaintainerCommand <Angle, Double> {

    public ClimberSubsystem climber;

    @Inject
    public ClimberMaintainer(ClimberSubsystem climber,
                             HumanVsMachineDecider.HumanVsMachineDeciderFactory hvmFactory,
                             PropertyFactory pf, PIDManager pidManager) {
        super(climber, pf, hvmFactory, 0.03, 0.1)
    }

    @Override
    protected void coastAction() {
        climber.stop();
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
        return 0.0; //No human input
    }

    @Override
    protected double getHumanInputMagnitude() {
        return 0; //No human input
    }
}
