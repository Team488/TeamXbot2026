package competition.simulation.shooter;

import competition.Robot;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.shooter_feeder.ShooterFeederSubsystem;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.system.plant.LinearSystemId;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.simulation.FlywheelSim;
import competition.simulation.intake.IntakeSimulator;
import org.ironmaple.simulation.seasonspecific.rebuilt2026.Arena2026Rebuilt;
import xbot.common.controls.actuators.mock_adapters.MockCANMotorController;
import xbot.common.math.PIDManager;
import xbot.common.math.PIDManager.PIDManagerFactory;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;
import xbot.common.simulation.MotorInternalPIDHelper;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.Random;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.Inches;
import static edu.wpi.first.units.Units.MetersPerSecond;
import static edu.wpi.first.units.Units.RPM;

@Singleton
public class ShooterSimulator {
    final PoseSubsystem pose;
    final ShooterSubsystem shooter;
    final ShooterFeederSubsystem shooterFeeder;

    final MockCANMotorController shooterMotor;
    final MockCANMotorController shooterFeederMotor;

    final IntakeSimulator intakeSimulator;

    final PIDManager pidManager;

    final DoubleProperty ballsPerSecond;
    final Random random;

    final DCMotor shooterGearBox = DCMotor.getKrakenX60(1);
    final FlywheelSim shooterSim;

    @Inject
    public ShooterSimulator(PoseSubsystem pose, ShooterSubsystem shooter, ShooterFeederSubsystem shooterFeeder,
                            PropertyFactory pf, IntakeSimulator intakeSimulator, PIDManagerFactory pidManagerFactory) {
        pf.setPrefix("Simulator/");
        this.pose = pose;
        this.shooter = shooter;
        this.shooterFeeder = shooterFeeder;

        this.shooterMotor = (MockCANMotorController) shooter.middleShooterMotor;
        this.shooterFeederMotor = (MockCANMotorController) shooterFeeder.shooterFeederMotor;

        this.intakeSimulator = intakeSimulator;

        this.pidManager = pidManagerFactory.create("ShooterSimulationPID"); // TODO: Add defaults

        this.ballsPerSecond = pf.createPersistentProperty("ballsPerSecond", 10);
        this.random = new Random();

        // TODO: Figure out these constants
        this.shooterSim = new FlywheelSim(
                LinearSystemId.createFlywheelSystem(
                        shooterGearBox,
                        0.0025,
                        1.0
                ),
                shooterGearBox
        );
    }

    public boolean isShooting() {
        // TODO: Change to shooter feeder once integrated
        return shooterMotor.getPower() > 0;
    }

    public AngularVelocity getShooterVelocity() {
        return this.shooterSim.getAngularVelocity();
    }

    public void update(Arena2026Rebuilt arena) {
        // Update the power of the motor
        MotorInternalPIDHelper.updateInternalPIDWithVelocity(
                this.shooterMotor, pidManager, RPM.of(shooter.targetVelocity.get()));

        // Update the sim
        if (DriverStation.isEnabled()) {
            this.shooterSim.setInputVoltage(this.shooterMotor.getPower() * RobotController.getBatteryVoltage());
        } else {
            this.shooterSim.setInputVoltage(0);
        }

        // Update the velocity of the motor using the sim
        this.shooterSim.update(Robot.LOOP_INTERVAL);
        this.shooterMotor.setVelocity(getShooterVelocity());

        if (isShooting() && random.nextDouble() < ballsPerSecond.get() / 50.0) {
            if (!intakeSimulator.getPieceFromIntake()) {
                return;
            }

            // TODO: Extract constants/magic-numbers later
            arena.addPieceWithVariance(
                    pose.getCurrentPose2d().getTranslation(),
                    pose.getCurrentHeading(),
                    Inches.of(20), // TODO: Shooter height
                    MetersPerSecond.of(10.0), // TODO: Angular velocity DIV flywheel radius
                    Degrees.of(80), // TODO: Hood
                    0.30,
                    0.30,
                    2.0,
                    1.0,
                    2.0
            );
        }
    }
}
