package competition.subsystems.shooter.commands;

import competition.subsystems.shooter.ShooterSubsystem;
import xbot.common.command.BaseCommand;
import xbot.common.controls.sensors.XTimer;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.Amps;

public class WaitForShootingFinished extends BaseCommand {
    private final ShooterSubsystem shooter;
    private final DoubleProperty timeToWaitForShootingToFinishSeconds;
    private boolean detectedShot;
    private double timeOfLastShot = Double.MAX_VALUE;

    @Inject
    public WaitForShootingFinished(ShooterSubsystem shooterSubsystem, PropertyFactory pf) {
        this.shooter = shooterSubsystem;

        pf.setPrefix(this);
        this.timeToWaitForShootingToFinishSeconds
                = pf.createPersistentProperty("Time To Wait For Shooting To Finish Seconds", 0.5);
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

        var shooterCurrentAboveThreshold = this.shooter.getHighestShooterCurrent().gt(Amps.of(this.shooter.currentDuringShootingThreshold.get()));
        if (shooterCurrentAboveThreshold) {
            this.detectedShot = true;
            this.timeOfLastShot = XTimer.getFPGATimestamp();
        }
    }

    @Override
    public boolean isFinished() {
        var timeSinceLastShot = XTimer.getFPGATimestamp() - this.timeOfLastShot;
        return this.detectedShot && timeSinceLastShot > this.timeToWaitForShootingToFinishSeconds.get();
    }
}
