package competition.simulation.shooter;

import competition.Robot;
import competition.simulation.SimulatorConstants;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.shooter_feeder.ShooterFeederSubsystem;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.system.plant.LinearSystemId;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.LinearVelocity;
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
import static edu.wpi.first.units.Units.Meters;
import static edu.wpi.first.units.Units.MetersPerSecond;
import static edu.wpi.first.units.Units.RPM;
import static edu.wpi.first.units.Units.RadiansPerSecond;

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

        this.pidManager = pidManagerFactory.create(
                pf.getPrefix() + "ShooterSimulationPID",
                0.2,
                0.001,
                0.0,
                0.0,
                1.0,
                -1.0
        ); // TODO: Adjust

        this.ballsPerSecond = pf.createPersistentProperty("ballsPerSecond", 10);
        this.random = new Random();

        // TODO: Figure out these constants
        this.shooterSim = new FlywheelSim(
                LinearSystemId.createFlywheelSystem(
                        shooterGearBox,
                        SimulatorConstants.flywheelJKgMetersSquared,
                        SimulatorConstants.flywheelGearing
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
        this.updateShooterSimAndMotor();
        this.updateShootingProjectile(arena);
    }

    public void updateShooterSimAndMotor() {
        // Update the power of the motor based on our current velocity and goal
        MotorInternalPIDHelper.updateInternalPIDWithVelocity(
                this.shooterMotor, pidManager, RPM.of(shooter.targetVelocity.get()));

        // Update the sim with our new power
        if (DriverStation.isEnabled()) {
            this.shooterSim.setInputVoltage(this.shooterMotor.getPower() * RobotController.getBatteryVoltage());
        } else {
            this.shooterSim.setInputVoltage(0);
        }
        this.shooterSim.update(Robot.LOOP_INTERVAL);

        // Reflect our new velocity in the sim to our motor
        this.shooterMotor.setVelocity(getShooterVelocity());
    }

    public void updateShootingProjectile(Arena2026Rebuilt arena) {
        if (isShooting() && random.nextDouble() < ballsPerSecond.get() / 50.0) {
            if (!intakeSimulator.getPieceFromIntake()) {
                return;
            }

            double speed = getShooterVelocity().in(RadiansPerSecond) * SimulatorConstants.flywheelRadius.in(Meters);
            arena.addPieceWithVariance(
                    pose.getCurrentPose2d().getTranslation(), // TODO: Offset so it is coming out from shooter
                    pose.getCurrentHeading(),
                    SimulatorConstants.shooterHeight,
                    MetersPerSecond.of(speed),
                    Degrees.of(80), // TODO: Read from hood sim
                    SimulatorConstants.positionVariance,
                    SimulatorConstants.positionVariance,
                    2.0,
                    1.0,
                    2.0
            );
        }
    }
}
