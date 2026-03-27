package competition.subsystems.drive;

import javax.inject.Inject;
import javax.inject.Singleton;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xbot.common.advantage.AKitLogger;
import xbot.common.advantage.DataFrameRefreshable;
import xbot.common.command.BaseRobot;
import xbot.common.injection.swerve.FrontLeftDrive;
import xbot.common.injection.swerve.FrontRightDrive;
import xbot.common.injection.swerve.RearLeftDrive;
import xbot.common.injection.swerve.RearRightDrive;
import xbot.common.injection.swerve.SwerveComponent;
import xbot.common.math.PIDDefaults;
import xbot.common.math.PIDManager.PIDManagerFactory;
import xbot.common.math.XYPair;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.Property;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.BaseSwerveDriveSubsystem;

import java.util.function.Supplier;

@Singleton
public class DriveSubsystem extends BaseSwerveDriveSubsystem implements DataFrameRefreshable {
    private static Logger log = LogManager.getLogger(DriveSubsystem.class);

    private Translation2d lookAtPointTarget = new Translation2d(); // The target point to look at
    private Rotation2d staticHeadingTarget = new Rotation2d(); // The heading you want to constantly be at
    private boolean lookAtPointActive = false;
    private boolean lookAtPointInverted = false;
    private boolean staticHeadingActive = false;
    private final DoubleProperty maxAutoTargetSpeedMps;
    private final DoubleProperty maxAutoFuelIntakeTargetSpeedMps;
    private final DoubleProperty interstitialSpeedMps;

    @Inject
    public DriveSubsystem(PIDManagerFactory pidFactory, PropertyFactory pf,
                          @FrontLeftDrive SwerveComponent frontLeftSwerve, @FrontRightDrive SwerveComponent frontRightSwerve,
                          @RearLeftDrive SwerveComponent rearLeftSwerve, @RearRightDrive SwerveComponent rearRightSwerve) {

        super(pidFactory, pf, frontLeftSwerve, frontRightSwerve, rearLeftSwerve, rearRightSwerve);
        log.info("Creating DriveSubsystem");

        pf.setPrefix(this.getPrefix());
        pf.setDefaultLevel(Property.PropertyLevel.Important);
        this.maxAutoTargetSpeedMps = pf.createPersistentProperty("MaxAutoTargetSpeedMetersPerSecond", 2.5);
        this.maxAutoFuelIntakeTargetSpeedMps = pf.createPersistentProperty("MaxAutoFuelIntakeTargetSpeedMetersPerSecond", 1.5);
        this.interstitialSpeedMps = pf.createPersistentProperty("InterstitialSpeedMetersPerSecond", 1);
    }

    @Override
    protected PIDDefaults getPositionalPIDDefaults() {
        return new PIDDefaults(
                1.08, // P
                0, // I
                4.0, // D
                0.0, // F
                0.6, // Max output
                -0.6, // Min output
                0.05, // Error threshold
                0.005, // Derivative threshold
                0.2); // Time threshold
    }

    @Override
    protected PIDDefaults getHeadingPIDDefaults() {
        var errorThreshold = BaseRobot.isSimulation() ? 5.0 : 2.0;
        return new PIDDefaults(
                0.0045, // P
                0.0001, // I
                0.0, // D
                0.0, // F
                0.75, // Max output
                -0.75, // Min output
                errorThreshold, // Error threshold
                0.2, // Derivative threshold
                0.2, // Time threshold
                10); // IZone
    }

    public Translation2d getLookAtPointTarget() {
        return lookAtPointTarget;
    }

    public Rotation2d getStaticHeadingTarget() {
        return staticHeadingTarget;
    }

    public boolean getLookAtPointActive() {
        return lookAtPointActive;
    }

    public boolean getStaticHeadingActive() {
        return staticHeadingActive;
    }

    public void setStaticHeadingTarget(Rotation2d staticHeadingTarget) {
        this.staticHeadingTarget = staticHeadingTarget;
    }

    public void setLookAtPointTarget(Translation2d lookAtPointTarget) {
        this.lookAtPointTarget = lookAtPointTarget;
    }

    public void setStaticHeadingTargetActive(boolean staticHeadingActive) {
        this.staticHeadingActive = staticHeadingActive;
    }

    public void setLookAtPointTargetActive(boolean lookAtPointActive) {
        this.lookAtPointActive = lookAtPointActive;
    }

    public void setLookAtPointInverted(boolean lookAtPointInverted) {
        this.lookAtPointInverted = lookAtPointInverted;
    }

    public boolean getLookAtPointInverted() {
        return lookAtPointInverted;
    }

    public double getMaxAutoTargetSpeedMetersPerSecond() {
        return this.maxAutoTargetSpeedMps.get();
    }

    public double getMaxAutoFuelIntakeTargetSpeedMetersPerSecond() {
        return this.maxAutoFuelIntakeTargetSpeedMps.get();
    }

    public double getInterstitialSpeedMetersPerSecond() {
        return this.interstitialSpeedMps.get();
    }

    public InstantCommand createSetStaticHeadingTargetCommand(Supplier<Rotation2d> staticHeadingTarget) {
        return new InstantCommand(() -> {
            setStaticHeadingTarget(staticHeadingTarget.get());
            setStaticHeadingTargetActive(true);}
        );
    }

    public InstantCommand createSetLookAtPointTargetCommand(Supplier<Translation2d> lookAtPointTarget) {
        return new InstantCommand(() -> {
            setLookAtPointTarget(lookAtPointTarget.get());
            setLookAtPointTargetActive(true);}
        );
    }

    public InstantCommand createClearAllHeadingTargetsCommand() {
        return new InstantCommand(() -> {
            setStaticHeadingTargetActive(false);
            setLookAtPointTargetActive(false);
        });
    }

    /** The follow methods are stole directly from Junjie's SCL PR which is probably 99.5% AI Generated */

    /**
     * Gets the current robot-relative chassis speeds by converting the current swerve module states
     * through inverse kinematics. This is needed by PathPlanner's AutoBuilder.
     * @return The current robot-relative ChassisSpeeds.
     */
    public ChassisSpeeds getRobotRelativeSpeeds() {
        var states = getCurrentSwerveStates();
        return getSwerveDriveKinematics().toChassisSpeeds(states.toArray());
    }

    /**
     * Drives the robot using the given robot-relative ChassisSpeeds. Converts the ChassisSpeeds
     * to individual swerve module states and applies them. This is needed by PathPlanner's AutoBuilder.
     * @param chassisSpeeds The desired robot-relative chassis speeds.
     */
    public void driveWithChassisSpeeds(ChassisSpeeds chassisSpeeds) {
        SwerveModuleState[] moduleStates = getSwerveDriveKinematics().toSwerveModuleStates(chassisSpeeds);
        SwerveDriveKinematics.desaturateWheelSpeeds(moduleStates, getMaxTargetSpeedMetersPerSecond());

        aKitLog.setLogLevel(AKitLogger.LogLevel.INFO);
        aKitLog.record("DesiredSwerveState", moduleStates);
        this.getFrontLeftSwerveModuleSubsystem().setTargetState(moduleStates[0]);
        this.getFrontRightSwerveModuleSubsystem().setTargetState(moduleStates[1]);
        this.getRearLeftSwerveModuleSubsystem().setTargetState(moduleStates[2]);
        this.getRearRightSwerveModuleSubsystem().setTargetState(moduleStates[3]);
    }
}
