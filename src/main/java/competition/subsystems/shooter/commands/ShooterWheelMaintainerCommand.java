package competition.subsystems.shooter.commands;

import competition.subsystems.shooter.ShooterSubsystem;
import edu.wpi.first.units.measure.AngularVelocity;
import xbot.common.command.BaseMaintainerCommand;
import xbot.common.logic.HumanVsMachineDecider;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.RPM;

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
        var targetValue = shooterWheel.getTrimmedTargetValue();
        if (targetValue.isEquivalent(RPM.of(0))) {
            shooterWheel.runMotorsAtVelocity(targetValue);
        } else {
            // When stopped, don't use PID
            shooterWheel.setPower(0.0);
        }
    }

    @Override
    protected double getErrorMagnitude() {
        var current = shooterWheel.getCurrentValue();
        var target = shooterWheel.getTargetValue();
        var error = target.minus(current);
        return error.in(RPM);
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
