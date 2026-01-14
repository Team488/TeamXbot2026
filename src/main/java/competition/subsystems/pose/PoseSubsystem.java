package competition.subsystems.pose;

import javax.inject.Inject;
import javax.inject.Singleton;

import competition.subsystems.drive.DriveSubsystem;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import xbot.common.controls.sensors.XGyro.XGyroFactory;
import xbot.common.math.WrappedRotation2d;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.pose.BasePoseSubsystem;

import java.util.Optional;

@Singleton
public class PoseSubsystem extends BasePoseSubsystem {

    final SwerveDrivePoseEstimator onlyWheelsGyroSwerveOdometry;
    protected Optional<SwerveModulePosition[]> simulatedModulePositions = Optional.empty();

    private final DriveSubsystem drive;

    @Inject
    public PoseSubsystem(XGyroFactory gyroFactory, PropertyFactory propManager, DriveSubsystem drive) {
        super(gyroFactory, propManager);
        this.drive = drive;

        onlyWheelsGyroSwerveOdometry = initializeSwerveOdometry();
    }


    @Override
    protected double getLeftDriveDistance() {
        return drive.getLeftTotalDistance();
    }

    @Override
    protected double getRightDriveDistance() {
        return drive.getRightTotalDistance();
    }


    private SwerveDrivePoseEstimator initializeSwerveOdometry() {
        return new SwerveDrivePoseEstimator(
                drive.getSwerveDriveKinematics(),
                getCurrentHeading(),
                getSwerveModulePositions(),
                new Pose2d());
    }

    @Override
    protected void updateOdometry() {
        onlyWheelsGyroSwerveOdometry.update(
                this.getCurrentHeading(),
                getSwerveModulePositions()
        );

        aKitLog.record("WheelsOnlyEstimate", onlyWheelsGyroSwerveOdometry.getEstimatedPosition());

        Translation2d positionSource = onlyWheelsGyroSwerveOdometry.getEstimatedPosition().getTranslation();
        Pose2d estimatedPosition = new Pose2d(
                positionSource,
                getCurrentHeading()
        );
        aKitLog.record("RobotPose", estimatedPosition);

        totalDistanceX = estimatedPosition.getX();
        totalDistanceY = estimatedPosition.getY();

        double prevTotalDistanceX = totalDistanceX;
        double prevTotalDistanceY = totalDistanceY;
        this.velocityX = ((totalDistanceX - prevTotalDistanceX));
        this.velocityY = ((totalDistanceY - prevTotalDistanceY));
        this.totalVelocity = (Math.sqrt(Math.pow(velocityX, 2.0) + Math.pow(velocityY, 2.0))); // Unnecessary?
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

    public void setCurrentPosition(double newXPositionMeters, double newYPositionMeters, WrappedRotation2d heading) {
        super.setCurrentPosition(newXPositionMeters, newYPositionMeters);
        super.setCurrentHeading(heading.getDegrees());
        onlyWheelsGyroSwerveOdometry.resetPosition(
                heading,
                getSwerveModulePositions(),
                new Pose2d(
                        newXPositionMeters,
                        newYPositionMeters,
                        this.getCurrentHeading()));
    }

    public void setCurrentPoseInMeters(Pose2d newPoseInMeters) {
        setCurrentPosition(
                newPoseInMeters.getTranslation().getX(),
                newPoseInMeters.getTranslation().getY(),
                WrappedRotation2d.fromRotation2d(newPoseInMeters.getRotation())
        );
    }

    // used by the physics simulator to mock what the swerve modules are doing
    // currently for pose estimation
    public void ingestSimulatedSwerveModulePositions(SwerveModulePosition[] positions) {
        this.simulatedModulePositions = Optional.of(positions);
    }
}