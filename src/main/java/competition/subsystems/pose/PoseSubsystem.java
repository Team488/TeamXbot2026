package competition.subsystems.pose;

import java.util.Optional;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import competition.electrical_contract.ElectricalContract;
// import competition.subsystems.deadwheel.DeadWheelSubsystem;             // no deadwheels currently
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.vision.AprilTagVisionSubsystemExtended;
// import competition.subsystems.vision.CoprocessorCommunicationSubsystem; // no coprocessor currently
import edu.wpi.first.math.estimator.PoseEstimator;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.units.measure.Distance;

import static edu.wpi.first.units.Units.Meters;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
// import org.kobe.xbot.JClient.XTablesClient;
// import org.kobe.xbot.Utilities.Entities.BatchedPushRequests;
import xbot.common.controls.sensors.XGyro.XGyroFactory;
import xbot.common.math.WrappedRotation2d;
// import xbot.common.math.estimator.DeadwheelPoseEstimator;
// import xbot.common.math.kinematics.DeadwheelKinematics;
// import xbot.common.math.kinematics.DeadwheelWheelPositions;
import xbot.common.properties.BooleanProperty;
import xbot.common.properties.Property;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.pose.BasePoseSubsystem;

@Singleton
public class PoseSubsystem extends BasePoseSubsystem {

    final SwerveDrivePoseEstimator onlyWheelsGyroSwerveOdometry;
    final SwerveDrivePoseEstimator fullSwerveOdometry;
    // final DeadwheelPoseEstimator onlyDeadwheelOdometry;
    // final DeadwheelPoseEstimator fullDeadwheelOdometry;

    private final DriveSubsystem drive;
    private final AprilTagVisionSubsystemExtended aprilTagVisionSubsystem;
    private final BooleanProperty useVisionAssistedPose;
    // private final BooleanProperty useDeadwheelAssistedPose;
    private final BooleanProperty continueUpdatingSwerveTelemetry;
    private final BooleanProperty reportCameraPoses;
    // private final CoprocessorCommunicationSubsystem coprocessorComms;  // no coprocessor currently

    private boolean preferOdometryToVision = false;
    // private final DeadWheelSubsystem deadWheelSubsystem;  // no deadwheels currently

    public static final Distance fieldXMidpointInMeters = Meters.of(8.7785);
    public static final Distance fieldYMidpointInMeters = Meters.of(4.025);

    protected Optional<SwerveModulePosition[]> simulatedModulePositions = Optional.empty();

    @Inject
    public PoseSubsystem(XGyroFactory gyroFactory,
            ElectricalContract electricalContract,
            PropertyFactory propManager, DriveSubsystem drive,
            AprilTagVisionSubsystemExtended aprilTagVisionSubsystem
            /* CoprocessorCommunicationSubsystem coprocessorComms,
            DeadWheelSubsystem deadWheelSubsystem */) {
        super(gyroFactory, propManager);
        this.drive = drive;
        this.aprilTagVisionSubsystem = aprilTagVisionSubsystem;
        // this.coprocessorComms = coprocessorComms;
        // this.deadWheelSubsystem = deadWheelSubsystem;

        this.onlyWheelsGyroSwerveOdometry = initializeSwerveOdometry();
        this.fullSwerveOdometry = initializeSwerveOdometry();
        // this.onlyDeadwheelOdometry = initializeDeadwheelOdometry();
        // this.fullDeadwheelOdometry = initializeDeadwheelOdometry();

        propManager.setPrefix(this);
        propManager.setDefaultLevel(Property.PropertyLevel.Important);
        useVisionAssistedPose = propManager.createPersistentProperty("UseVisionAssistedPose", true);
        // useDeadwheelAssistedPose = propManager.createPersistentProperty("useDeadwheelAssistedPose", false);
        continueUpdatingSwerveTelemetry = propManager.createPersistentProperty("continueUpdatingSwerveTelemetry", true);
        reportCameraPoses = propManager.createPersistentProperty("ReportCameraPoses", false);
    }

    // private DeadwheelPoseEstimator initializeDeadwheelOdometry() {
    //     return new DeadwheelPoseEstimator(new DeadwheelKinematics(1), getCurrentHeadingGyroOnly(),
    //             this.deadWheelSubsystem.getLeftAdjustedDistance().in(Meters),
    //             this.deadWheelSubsystem.getRightAdjustedDistance().in(Meters),
    //             this.deadWheelSubsystem.getFrontAdjustedDistance().in(Meters),
    //             this.deadWheelSubsystem.getRearAdjustedDistance().in(Meters),
    //             new Pose2d());
    // }

    private SwerveDrivePoseEstimator initializeSwerveOdometry() {
        return new SwerveDrivePoseEstimator(
                drive.getSwerveDriveKinematics(),
                getCurrentHeadingGyroOnly(),
                getSwerveModulePositions(),
                new Pose2d());
    }
    @SuppressWarnings("unchecked")
    private <T> PoseEstimator<T> getPrimaryPoseEstimator() {
        // return (PoseEstimator<T>) (this.useDeadwheelAssistedPose.get()
        //         ? this.fullDeadwheelOdometry
        //         : this.fullSwerveOdometry);
        return (PoseEstimator<T>) this.fullSwerveOdometry;
    }
    @SuppressWarnings("unchecked")
    private <T> PoseEstimator<T> getPrimaryOdometryOnlyPoseEstimator() {
        // return (PoseEstimator<T>) (this.useDeadwheelAssistedPose.get()
        //         ? this.onlyWheelsGyroSwerveOdometry
        //         : this.onlyDeadwheelOdometry);
        return (PoseEstimator<T>) this.onlyWheelsGyroSwerveOdometry;
    }

    private boolean shouldAlsoUpdateFullSwerve() {
        return /* this.useDeadwheelAssistedPose.get() && */ this.continueUpdatingSwerveTelemetry.get();
    }

    private void updateOdometryWithVision() {
        // Also update full swerve if we should add swerve and it's not already being
        // updated.
        this.aprilTagVisionSubsystem.getAllPoseObservations().forEach(observation -> {
            this.getPrimaryPoseEstimator().addVisionMeasurement(
                    observation.visionRobotPoseMeters(),
                    observation.timestampSeconds(),
                    observation.visionMeasurementStdDevs());

            if (this.shouldAlsoUpdateFullSwerve()) {
                this.fullSwerveOdometry.addVisionMeasurement(
                        observation.visionRobotPoseMeters(),
                        observation.timestampSeconds(),
                        observation.visionMeasurementStdDevs());
            }
        });
    }

    @Override
    protected void updateOdometry() {
        // Update pose estimators
        onlyWheelsGyroSwerveOdometry.update(
                this.getCurrentHeadingGyroOnly(),
                getSwerveModulePositions());

        // DeadWheel pose estimator - no deadwheels currently
        // deadWheelSubsystem.update();

        // if (this.useDeadwheelAssistedPose.get()) {
        //     fullDeadwheelOdometry.update(
        //             this.getCurrentHeadingGyroOnly(),
        //             this.deadWheelSubsystem.getLeftAdjustedDistance().in(Meters),
        //             this.deadWheelSubsystem.getRightAdjustedDistance().in(Meters),
        //             this.deadWheelSubsystem.getFrontAdjustedDistance().in(Meters),
        //             this.deadWheelSubsystem.getRearAdjustedDistance().in(Meters));

        // }
        // if (!this.useDeadwheelAssistedPose.get() || this.shouldAlsoUpdateFullSwerve()) {
        //     this.fullSwerveOdometry.update(
        //             this.getCurrentHeadingGyroOnly(),
        //             getSwerveModulePositions());
        // }

        // this.onlyDeadwheelOdometry.update(
        //         this.getCurrentHeading(),
        //         getDeadwheelPositions());

        this.updateOdometryWithVision();

        aKitLog.record("WheelsOnlyEstimate", onlyWheelsGyroSwerveOdometry.getEstimatedPosition());
        // aKitLog.record("DeadwheelOnlyEstimate", onlyDeadwheelOdometry.getEstimatedPosition());
        // aKitLog.record("FullVisionDeadwheelEstimate", fullDeadwheelOdometry.getEstimatedPosition());

        // Report poses
        Pose2d swerveOnlyPosition = new Pose2d(
                onlyWheelsGyroSwerveOdometry.getEstimatedPosition().getTranslation(),
                getCurrentHeadingGyroOnly());
        aKitLog.record("OdometryOnlyRobotPose", swerveOnlyPosition);

        Pose2d fullSwervePosition = new Pose2d(
                fullSwerveOdometry.getEstimatedPosition().getTranslation(),
                fullSwerveOdometry.getEstimatedPosition().getRotation());
        aKitLog.record("SwerveVisionEnhancedPose", fullSwervePosition);

        Pose2d visionEnhancedPosition = new Pose2d(
                this.getPrimaryPoseEstimator().getEstimatedPosition().getTranslation(),
                this.getPrimaryPoseEstimator().getEstimatedPosition().getRotation());
        aKitLog.record("VisionEnhancedPose", visionEnhancedPosition);

        // Pose2d deadWheelPosition = fullDeadwheelOdometry.getEstimatedPosition();
        // aKitLog.record("DeadWheelPosition", deadWheelPosition);

        Pose2d robotPose = this.useVisionAssistedPose.get() && !preferOdometryToVision
                ? getPrimaryPoseEstimator().getEstimatedPosition()
                : getPrimaryOdometryOnlyPoseEstimator().getEstimatedPosition();

        aKitLog.record("RobotPose", robotPose);

        // Record the camera positions
        if (reportCameraPoses.get()) {
            var robotPose3d = new Pose3d(
                    robotPose.getTranslation().getX(),
                    robotPose.getTranslation().getY(),
                    -0.5, // Reverse offset in Advantage Scope
                    new Rotation3d(robotPose.getRotation()));
            for (int i = 0; i < aprilTagVisionSubsystem.getCameraCount(); i++) {
                var cameraPosition = aprilTagVisionSubsystem.getCameraPosition(i);
                aKitLog.record("CameraPose/" + i, robotPose3d.transformBy(cameraPosition));
            }
        }

        totalDistanceX = robotPose.getX();
        totalDistanceY = robotPose.getY();

        double prevTotalDistanceX = totalDistanceX;
        double prevTotalDistanceY = totalDistanceY;
        this.velocityX = ((totalDistanceX - prevTotalDistanceX));
        this.velocityY = ((totalDistanceY - prevTotalDistanceY));
        this.totalVelocity = (Math.sqrt(Math.pow(velocityX, 2.0) + Math.pow(velocityY, 2.0))); // Unnecessary?
    }

    public double getAbsoluteVelocity() {
        return this.totalVelocity;
    }

    /**
     * Get a command that resets the pose estimator to the current vision estimate
     * 
     * @return The command that resets the pose estimator
     */
    public Command getResetTranslationToVisionEstimateCommand() {
        return new InstantCommand(() -> {
            var estimatedPose = new Pose2d(
                    this.getPrimaryPoseEstimator().getEstimatedPosition().getTranslation(),
                    getCurrentHeadingGyroOnly());
            resetPoseEstimator(estimatedPose);
        }).ignoringDisable(true);
    }

    /**
     * Get a command that resets the pose estimator to a specific pose
     * 
     * @param pose The pose to reset the estimator to
     * @return The command that resets the pose estimator
     */
    public Command getResetPoseCommand(Pose2d pose) {
        return new InstantCommand(() -> resetPoseEstimator(pose))
                .ignoringDisable(true);
    }

    private void resetPoseEstimator(Pose2d pose) {
        this.fullSwerveOdometry.resetPose(pose);
        // this.fullDeadwheelOdometry.resetPose(pose);
        this.onlyWheelsGyroSwerveOdometry.resetPose(pose);
        // this.onlyDeadwheelOdometry.resetPose(pose);
        // this.deadWheelSubsystem.resetPose(pose);
    }

    private SwerveModulePosition[] getSwerveModulePositions() {
        // if we have simulated data, return that directly instead of asking the
        // modules
        if (simulatedModulePositions.isPresent()) {
            return simulatedModulePositions.get();
        }
        return new SwerveModulePosition[] {
                drive.getFrontLeftSwerveModuleSubsystem().getCurrentPosition(),
                drive.getFrontRightSwerveModuleSubsystem().getCurrentPosition(),
                drive.getRearLeftSwerveModuleSubsystem().getCurrentPosition(),
                drive.getRearRightSwerveModuleSubsystem().getCurrentPosition()

        };
    }

    // no deadwheels currently
    // private DeadwheelWheelPositions getDeadwheelPositions() {
    //     return new DeadwheelWheelPositions(
    //             this.deadWheelSubsystem.getLeftAdjustedDistance(),
    //             this.deadWheelSubsystem.getRightAdjustedDistance(),
    //             this.deadWheelSubsystem.getFrontAdjustedDistance(),
    //             this.deadWheelSubsystem.getRearAdjustedDistance());
    // }

    // Override methods remain unchanged

    @Override
    protected double getLeftDriveDistance() {
        return drive.getLeftTotalDistance();
    }

    @Override
    protected double getRightDriveDistance() {
        return drive.getRightTotalDistance();
    }

    public void setCurrentPosition(double newXPositionMeters, double newYPositionMeters, WrappedRotation2d heading) {
        super.setCurrentPosition(newXPositionMeters, newYPositionMeters);
        super.setCurrentHeading(heading.getDegrees());
        onlyWheelsGyroSwerveOdometry.resetPosition(
                heading,
                getSwerveModulePositions(),
                new Pose2d(
                        newXPositionMeters,
                        newYPositionMeters,
                        this.getCurrentHeadingGyroOnly()));
        fullSwerveOdometry.resetPosition(
                heading,
                getSwerveModulePositions(),
                new Pose2d(
                        newXPositionMeters,
                        newYPositionMeters,
                        this.getCurrentHeadingGyroOnly()));

        // no deadwheels currently
        //
        // var deadwheelPositions = new DeadwheelWheelPositions(
        //         this.deadWheelSubsystem.getLeftAdjustedDistance(),
        //         this.deadWheelSubsystem.getRightAdjustedDistance(),
        //         this.deadWheelSubsystem.getFrontAdjustedDistance(),
        //         this.deadWheelSubsystem.getRearAdjustedDistance());
        //
        // this.onlyDeadwheelOdometry.resetPosition(
        //         heading,
        //         deadwheelPositions,
        //         new Pose2d(
        //                 newXPositionMeters,
        //                 newYPositionMeters,
        //                 this.getCurrentHeadingGyroOnly()));
        // fullDeadwheelOdometry.resetPosition(
        //         heading,
        //         deadwheelPositions,
        //         new Pose2d(
        //                 newXPositionMeters,
        //                 newYPositionMeters,
        //                 this.getCurrentHeadingGyroOnly()));
    }

    public void setCurrentPosition(Pose2d pose) {
        setCurrentPosition(pose.getTranslation().getX(), pose.getTranslation().getY(),
                WrappedRotation2d.fromRotation2d(pose.getRotation()));
    }

    public void setCurrentPoseInMeters(Pose2d newPoseInMeters) {
        setCurrentPosition(
                newPoseInMeters.getTranslation().getX(),
                newPoseInMeters.getTranslation().getY(),
                WrappedRotation2d.fromRotation2d(newPoseInMeters.getRotation()));
    }

    @Override
    public Pose2d getCurrentPose2d() {
        return useVisionAssistedPose.get() ? new Pose2d(
                this.getPrimaryPoseEstimator().getEstimatedPosition().getTranslation(),
                this.getPrimaryPoseEstimator().getEstimatedPosition().getRotation())
                : new Pose2d(
                        this.getPrimaryOdometryOnlyPoseEstimator().getEstimatedPosition().getTranslation(),
                        this.getPrimaryOdometryOnlyPoseEstimator().getEstimatedPosition().getRotation());
    }

    @Override
    public WrappedRotation2d getCurrentHeading() {
        if (useVisionAssistedPose.get()) {
            return WrappedRotation2d
                    .fromRotation2d(this.getPrimaryPoseEstimator().getEstimatedPosition().getRotation());
        } else {
            return WrappedRotation2d
                    .fromRotation2d(this.getPrimaryOdometryOnlyPoseEstimator().getEstimatedPosition().getRotation());
        }
    }

    // used by the physics simulator to mock what the swerve modules are doing
    // currently for pose estimation
    public void ingestSimulatedSwerveModulePositions(SwerveModulePosition[] positions) {
        this.simulatedModulePositions = Optional.of(positions);
    }

    public Command createSetPositionCommand(Pose2d pose) {
        return Commands.runOnce(() -> setCurrentPosition(pose));
    }

    public Command createSetPositionCommand(Supplier<Pose2d> poseSupplier) {
        return Commands.runOnce(() -> setCurrentPosition(poseSupplier.get())).ignoringDisable(true);
    }

    public Command createSetPositionCommandThatMirrorsIfNeeded(Pose2d bluePose) {
        return Commands.runOnce(() -> setCurrentPosition(PoseSubsystem.convertBlueToRedIfNeeded(bluePose)))
                .ignoringDisable(true);
    }

    public void setPreferOdometryToVision(boolean preferOdometryToVision) {
        this.preferOdometryToVision = preferOdometryToVision;
        if (preferOdometryToVision) {
            // If we are disabling vision updates, we need to "snap" the odometry estimate
            // to the vision estimate.
            // This is because we will be using the odometry estimate while vision is being
            // supresse, and we need
            // to avoid any callers of the PoseSubsystem experiencing discontinuities.
            resetPoseEstimator(getPrimaryPoseEstimator().getEstimatedPosition());
        }
    }
}
