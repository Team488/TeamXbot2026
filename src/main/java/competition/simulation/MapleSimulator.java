package competition.simulation;

import competition.Robot;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Distance;
import org.ironmaple.simulation.drivesims.COTS;
import xbot.common.advantage.AKitLogger;
import xbot.common.controls.sensors.mock_adapters.MockGyro;
import xbot.common.logic.TimeStableValidator;

import static edu.wpi.first.units.Units.Meters;
import static edu.wpi.first.units.Units.Seconds;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ironmaple.simulation.SimulatedArena;
import org.ironmaple.simulation.drivesims.SelfControlledSwerveDriveSimulation;
import org.ironmaple.simulation.drivesims.SwerveDriveSimulation;
import org.ironmaple.simulation.drivesims.configs.DriveTrainSimulationConfig;

@Singleton
public class MapleSimulator implements BaseSimulator {
    final PoseSubsystem pose;
    final DriveSubsystem drive;

    protected final AKitLogger aKitLog;

    final Distance humanLoadingDistanceThreshold = Meters.of(0.2);
    final TimeStableValidator humanLoadValidator = new TimeStableValidator(1);

    // maple-sim stuff ----------------------------
    final DriveTrainSimulationConfig config;
    final SimulatedArena arena;
    final SelfControlledSwerveDriveSimulation swerveDriveSimulation;

    @Inject
    public MapleSimulator(PoseSubsystem pose, DriveSubsystem drive) {
        this.pose = pose;
        this.drive = drive;

        aKitLog = new AKitLogger("Simulator/");

        /**
         * MapleSim arena and drive setup
         */
        arena = SimulatedArena.getInstance();
        arena.resetFieldForAuto();

        var ourConfig = new DriveTrainSimulationConfig(
                Units.Kilograms.of((double)45.0F),
                Units.Meters.of(0.76),
                Units.Meters.of(0.76),
                Units.Meters.of(0.52),
                Units.Meters.of(0.52),
                COTS.ofMark4(
                        DCMotor.getKrakenX60(1),
                        DCMotor.getKrakenX60(1),
                        COTS.WHEELS.SLS_PRINTED_WHEELS.cof,
                        3),
                COTS.ofPigeon2());

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

        SimulatedArena.overrideSimulationTimings(Seconds.of(Robot.LOOP_INTERVAL), 5);
    }

    public void update() {
        this.updateDriveSimulation();
    }

    protected void updateDriveSimulation() {
        // drive simulated robot from requested robot commands
        swerveDriveSimulation.runSwerveStates(drive.getTargetSwerveStates().toArray());

        // run the simulation
        arena.simulationPeriodic();
        swerveDriveSimulation.periodic();

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