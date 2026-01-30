package competition.subsystems.shooter.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.shooter_feeder.ShooterFeederSubsystem;
import edu.wpi.first.units.measure.Angle;
import xbot.common.command.BaseMaintainerCommand;
import xbot.common.command.BaseSetpointCommand;
import xbot.common.command.BaseSetpointSubsystem;
import xbot.common.logic.HumanVsMachineDecider;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import java.lang.annotation.Target;

public class ShooterMaintainerCommand extends BaseMaintainerCommand<Angle, Double> {

    private ShooterSubsystem shooter;
    private DoubleProperty ShooterMaxPower;
    private DoubleProperty ShooterMinPower;
    private OperatorInterface oi;


    @Inject
    public ShooterMaintainerCommand(ShooterSubsystem shooterSubsystem, PropertyFactory pf, HumanVsMachineDecider.HumanVsMachineDeciderFactory
                                    hvmFactory, BaseSetpointSubsystem baseSetpointSubsystem) {
        super(baseSetpointSubsystem, pf, hvmFactory, 0.1, 0.1); // tweak number
        pf.setPrefix(this);
        this.oi = oi;
        this.shooter = shooterSubsystem;

        ShooterMaxPower = pf.createPersistentProperty("ShooterMaxPower", 2); // tweak number
        ShooterMinPower = pf.createPersistentProperty("ShooterMinPower", 2); // tweak number

    }


    @Override
    protected void coastAction() {
        shooter.setPower(0); //fix
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
        return null;
    }

    @Override
    protected double getHumanInputMagnitude() {
        return 0;
    }
}
