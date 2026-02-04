package competition.subsystems.shooter.commands;

import competition.subsystems.shooter.ShooterSubsystem;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.AngularVelocity;
import xbot.common.command.BaseMaintainerCommand;
import xbot.common.logic.HumanVsMachineDecider;
import xbot.common.math.PID;
import xbot.common.math.PIDManager;
import xbot.common.math.PIDManager_Factory;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

public class ShooterWheelMaintainerCommand extends BaseMaintainerCommand<AngularVelocity, Double> {

    final ShooterSubsystem shooterWheel;

    @Inject
    public ShooterWheelMaintainerCommand(ShooterSubsystem shooterWheel, PropertyFactory pf,
                                         HumanVsMachineDecider.HumanVsMachineDeciderFactory hvmFactory) {
        super(shooterWheel, pf, hvmFactory, 0.01, 0.01); // tweak number
        this.shooterWheel = shooterWheel;
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    protected void coastAction() {
        shooterWheel.stop();
    }

    @Override
    protected void calibratedMachineControlAction() {
        shooterWheel.runAtTargetVelocity();
    }

    @Override
    protected double getErrorMagnitude() {
        var current = shooterWheel.getCurrentValue();
        var target = shooterWheel.getTargetValue();
        var error = target.minus(current);
        return error.in(Units.RPM);
    }

    @Override
    protected Double getHumanInput() {
        return 0.0;
    }

    @Override
    protected double getHumanInputMagnitude() {
        return Math.abs(getHumanInput());
        // magnitude = distance/absolute value
    }
    @Override
    public void end(boolean interrupted) {
        if (interrupted) {
            shooterWheel.stop();
        }
    }
}
