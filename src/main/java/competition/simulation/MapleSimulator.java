package competition.simulation;

import competition.Robot;
import competition.simulation.shooter.ShooterSimulator;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Distance;
import org.ironmaple.simulation.drivesims.COTS;
import org.ironmaple.simulation.seasonspecific.rebuilt2026.Arena2026Rebuilt;
import xbot.common.advantage.AKitLogger;
import xbot.common.controls.sensors.mock_adapters.MockGyro;
import xbot.common.logic.TimeStableValidator;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.Inches;
import static edu.wpi.first.units.Units.Meters;
import static edu.wpi.first.units.Units.MetersPerSecond;
import static edu.wpi.first.units.Units.Seconds;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ironmaple.simulation.IntakeSimulation;
import org.ironmaple.simulation.SimulatedArena;
import org.ironmaple.simulation.drivesims.SelfControlledSwerveDriveSimulation;
import org.ironmaple.simulation.drivesims.SwerveDriveSimulation;
import org.ironmaple.simulation.drivesims.configs.DriveTrainSimulationConfig;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import java.util.Random;

@Singleton
public class MapleSimulator implements BaseSimulator {
    final PoseSubsystem pose;
    final DriveSubsystem drive;

    protected final AKitLogger aKitLog;

    final Distance humanLoadingDistanceThreshold = Meters.of(0.2);
    final TimeStableValidator humanLoadValidator = new TimeStableValidator(1);

    // maple-sim stuff ----------------------------
    final DriveTrainSimulationConfig config;
    final IntakeSimulation intakeSimulation;
    final Arena2026Rebuilt arena;
    final SelfControlledSwerveDriveSimulation swerveDriveSimulation;

    final ShooterSimulator shooterSimulator;

    final DoubleProperty ballsPerSecond;

    private final Random random;

    @Inject
    public MapleSimulator(PoseSubsystem pose, DriveSubsystem drive, ShooterSimulator shooter, PropertyFactory pf) {
        pf.setPrefix("Simulator/");
        this.pose = pose;
        this.drive = drive;

        this.shooterSimulator = shooter;
        this.ballsPerSecond = pf.createPersistentProperty("ballsPerSecond", 10);

        aKitLog = new AKitLogger("Simulator/");

        /**
         * MapleSim arena and drive setup
         */
        arena = new org.ironmaple.simulation.seasonspecific.rebuilt2026.Arena2026Rebuilt(false);
        SimulatedArena.overrideInstance(arena);
        
        // uncomment this to force all fuel onto the sim, otherwise it defaults to 1/3rd of the fuel for perf reasons
        //((Arena2026Rebuilt)arena).setEfficiencyMode(false);
        arena.resetFieldForAuto();

        var ourConfig = new DriveTrainSimulationConfig(
                Units.Kilograms.of((double)45.0F),
                Units.Meters.of(0.76),
                Units.Meters.of(0.76),
                Units.Meters.of(0.52),
                Units.Meters.of(0.52),
                COTS.ofPigeon2(),
                COTS.ofMark4(
                        DCMotor.getKrakenX60(1),
                        DCMotor.getKrakenX60(1),
                        COTS.WHEELS.SLS_PRINTED_WHEELS.cof,
                        3)
        );

        // TODO: custom things to provide here like motor ratios and what have you
        config = ourConfig.withCustomModuleTranslations(new Translation2d[] {
                drive.getFrontLeftSwerveModuleSubsystem().getModuleTranslation(),
                drive.getFrontRightSwerveModuleSubsystem().getModuleTranslation(),
                drive.getRearLeftSwerveModuleSubsystem().getModuleTranslation(),
                drive.getRearRightSwerveModuleSubsystem().getModuleTranslation()
        });

        // starting middle ish of the field on blue
        var startingPose = new Pose2d(7, 7 , new Rotation2d());

        // Creating the SelfControlledSwerveDriveSimulation instance
        this.swerveDriveSimulation = new SelfControlledSwerveDriveSimulation(
                new SwerveDriveSimulation(config, startingPose));
        // Tell the robot it's starting in the same spot
        pose.setCurrentPoseInMeters(startingPose);

        arena.addDriveTrainSimulation(swerveDriveSimulation.getDriveTrainSimulation());

        intakeSimulation = IntakeSimulation.OverTheBumperIntake(
            "Fuel",
            this.swerveDriveSimulation.getDriveTrainSimulation(),
            // How big the intake is
            Inches.of(28),
            Inches.of(12),
            IntakeSimulation.IntakeSide.FRONT,
            100
        );

        // TODO: this should depend on when we actually deploy and run our collector
        // but for now just auto deploy it right away
        intakeSimulation.startIntake();

        SimulatedArena.overrideSimulationTimings(Seconds.of(Robot.LOOP_INTERVAL), 5);

        this.random = new Random();
    }

    public void update() {
        this.updateDriveSimulation();
        this.updateShooterSimulation();
    }

    protected void updateShooterSimulation() {
        if (!this.shooterSimulator.isShooting()) {
            return;
        }

        // TODO: Add a count for # of fuel stored in robot so we don't go crazy
        // TODO: Extract constants later
        if (random.nextDouble() < ballsPerSecond.get() / 50.0) {
            // Note: All magic numbers here are generated by ChatGPT
            arena.addPieceWithVariance(
                    pose.getCurrentPose2d().getTranslation(),
                    pose.getCurrentHeading(),
                    Inches.of(20),
                    MetersPerSecond.of(12.0),
                    Degrees.of(65),
                    0.30,
                    0.30,
                    2.0,
                    1.0,
                    2.0
            );
        }
    }

    protected void updateDriveSimulation() {
        // drive simulated robot from requested robot commands
        swerveDriveSimulation.runSwerveStates(drive.getTargetSwerveStates().toArray());

        // run the simulation
        arena.simulationPeriodic();
        swerveDriveSimulation.periodic();

        aKitLog.record("FieldSimulation/GamePieces", arena.getGamePiecesArrayByType("Fuel"));
        

        // this is where the robot really is in the sim
        aKitLog.record("FieldSimulation/Robot", swerveDriveSimulation.getActualPoseInSimulationWorld());

        // tell the pose subystem about where the robot has moved based on odometry
        pose.ingestSimulatedSwerveModulePositions(swerveDriveSimulation.getLatestModulePositions());

        aKitLog.record("RobotVelocity", swerveDriveSimulation.getActualSpeedsFieldRelative());

        // update gyro reading from sim
        ((MockGyro) pose.imu).setYaw(this.swerveDriveSimulation.getOdometryEstimatedPose().getRotation().getMeasure());
    }

    @Override
    public void resetPosition(Pose2d pose) {
        arena.resetFieldForAuto();
        this.swerveDriveSimulation.getDriveTrainSimulation().setSimulationWorldPose(pose);
        this.pose.setCurrentPoseInMeters(pose);
    }

    @Override
    public Pose2d getGroundTruthPose() {
        return this.swerveDriveSimulation.getActualPoseInSimulationWorld();
    }
}