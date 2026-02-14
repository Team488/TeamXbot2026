package competition.subsystems.shooter_feeder.commands;

import competition.subsystems.shooter_feeder.ShooterFeederSubsystem;
import xbot.common.command.BaseCommand;
import xbot.common.properties.DoubleProperty;
import edu.wpi.first.math.filter.SlewRateLimiter;
import javax.inject.Inject;

public class FixShooterCommand extends BaseCommand {

    private final ShooterFeederSubsystem shooterFeeder;
    private final SlewRateLimiter ramp;
    private DoubleProperty targetPower;

    @Inject
    public FixShooterCommand(ShooterFeederSubsystem shooterFeeder) {
        this.shooterFeeder = shooterFeeder;

        this.ramp = new SlewRateLimiter(1.0);

        this.targetPower = shooterFeeder.shooterFeederMotorPower;

        this.addRequirements(shooterFeeder);
    }

    public void setTargetPower(DoubleProperty powerProp) {
        this.targetPower = powerProp;
    }

    @Override
    public void initialize() {
        log.info("Initializing FixShooterCommand");
        ramp.reset(0);
    }

    @Override
    public void execute() {
        if (shooterFeeder.shooterFeederMotor != null && targetPower != null) {
            double smoothedPower = ramp.calculate(targetPower.get());
            shooterFeeder.shooterFeederMotor.setPower(smoothedPower);
        }
    }

    @Override
    public void end(boolean interrupted) {
        if (shooterFeeder.shooterFeederMotor != null) {
            shooterFeeder.shooterFeederMotor.setPower(0);
        }
    }
}

