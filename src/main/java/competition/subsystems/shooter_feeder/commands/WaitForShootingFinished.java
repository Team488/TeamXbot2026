package competition.subsystems.shooter_feeder.commands;

import competition.subsystems.shooter_feeder.ShooterFeederSubsystem;
import xbot.common.command.BaseCommand;
import xbot.common.controls.sensors.XTimer;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.Amps;

public class WaitForShootingFinished extends BaseCommand {
    private final ShooterFeederSubsystem shooterFeeder;
    private final DoubleProperty timeToWaitAfterCurrentSettles;
    private final DoubleProperty currentThreshold;
    private boolean detectedShot;
    private double timeOfLastShot = Double.MAX_VALUE;

    @Inject
    public WaitForShootingFinished(ShooterFeederSubsystem shooterFeeder, PropertyFactory pf) {
        this.shooterFeeder = shooterFeeder;

        pf.setPrefix(this);
        this.timeToWaitAfterCurrentSettles
                = pf.createPersistentProperty("Time to Wait After Current Threshold Reached Seconds", 0.5);
        this.currentThreshold = pf.createPersistentProperty("Current Threshold Amps", 10.0);
    }

    @Override
    public void initialize() {
        super.initialize();
        this.detectedShot = false;
        this.timeOfLastShot = Double.MAX_VALUE;
    }

    @Override
    public void execute() {
        super.execute();

        var feederCurrentAboveThreshold = this.shooterFeeder.getMotorCurrent().gt(Amps.of(this.currentThreshold.get()));
        if (feederCurrentAboveThreshold) {
            this.detectedShot = true;
            this.timeOfLastShot = XTimer.getFPGATimestamp();
        }
    }

    @Override
    public boolean isFinished() {
        var timeSinceLastShot = XTimer.getFPGATimestamp() - this.timeOfLastShot;
        return this.detectedShot && timeSinceLastShot > this.timeToWaitAfterCurrentSettles.get();
    }
}
