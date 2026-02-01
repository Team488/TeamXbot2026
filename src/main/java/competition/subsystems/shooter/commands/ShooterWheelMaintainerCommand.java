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
    final DoubleProperty humanMaxPower;
    final DoubleProperty humanMinPower;
    final PIDManager pidManager;

    @Inject
    public ShooterWheelMaintainerCommand(ShooterSubsystem shooterWheel, PropertyFactory pf,
                                         HumanVsMachineDecider.HumanVsMachineDeciderFactory hvmFactory,
                                         PIDManager.PIDManagerFactory pidManagerFactory) {
        super(shooterWheel, pf, hvmFactory, 0.01, 0.01); // tweak number
        pf.setPrefix(this);
        this.shooterWheel = shooterWheel;
        this.pidManager = pidManagerFactory.create(
                pf.getPrefix() + "/ShooterWheelMaintainerPID",
                0,
                0,
                0,
                0,
                0,
                0
        );
        humanMaxPower = pf.createPersistentProperty("ShooterMaxPower", 0.2); // tweak number
        humanMinPower = pf.createPersistentProperty("ShooterMinPower", -0.2); // tweak number

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
        //use pid to reach target
        double error = getErrorMagnitude();
        double power = pidManager.calculate(
                shooterWheel.getCurrentValue().in(Units.RPM),
                shooterWheel.getTargetValue().in(Units.RPM)
        );

        double max = humanMaxPower.get();
        double min = humanMinPower.get();
        shooterWheel.setPower(power);
        //don't need to calibrate since it will just keep rotating
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
