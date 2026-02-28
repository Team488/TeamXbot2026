package competition.subsystems.drive;

import javax.inject.Inject;
import javax.inject.Singleton;

import competition.electrical_contract.ElectricalContract;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xbot.common.advantage.AKitLogger;
import xbot.common.advantage.DataFrameRefreshable;
import xbot.common.controls.actuators.XCANMotorController;
import xbot.common.injection.swerve.FrontLeftDrive;
import xbot.common.injection.swerve.FrontRightDrive;
import xbot.common.injection.swerve.RearLeftDrive;
import xbot.common.injection.swerve.RearRightDrive;
import xbot.common.injection.swerve.SwerveComponent;
import xbot.common.math.PIDManager.PIDManagerFactory;
import xbot.common.properties.Property;
import xbot.common.properties.PropertyFactory;
import xbot.common.properties.XPropertyManager;
import xbot.common.subsystems.drive.BaseSwerveDriveSubsystem;

import java.util.function.Supplier;

@Singleton
public class DriveSubsystem extends BaseSwerveDriveSubsystem implements DataFrameRefreshable {
    private static Logger log = LogManager.getLogger(DriveSubsystem.class);

    private Translation2d lookAtPointTarget = new Translation2d(); // The target point to look at
    private Rotation2d staticHeadingTarget = new Rotation2d(); // The heading you want to constantly be at
    private boolean lookAtPointActive = false;
    private boolean staticHeadingActive = false;

    @Inject
    public DriveSubsystem(PIDManagerFactory pidFactory, PropertyFactory pf,
                          @FrontLeftDrive SwerveComponent frontLeftSwerve, @FrontRightDrive SwerveComponent frontRightSwerve,
                          @RearLeftDrive SwerveComponent rearLeftSwerve, @RearRightDrive SwerveComponent rearRightSwerve) {

        super(pidFactory, pf, frontLeftSwerve, frontRightSwerve, rearLeftSwerve, rearRightSwerve);
        log.info("Creating DriveSubsystem");

        pf.setPrefix(this.getPrefix());
        pf.setDefaultLevel(Property.PropertyLevel.Important);
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
}
