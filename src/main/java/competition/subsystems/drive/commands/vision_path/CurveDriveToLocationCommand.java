package competition.subsystems.drive.commands.vision_path;

import xbot.common.subsystems.oracle.SwervePointPathPlanning;
import competition.subsystems.pose.Landmarks;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.vision.AprilTagVisionSubsystemExtended;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;
import xbot.common.logging.RobotAssertionManager;
import xbot.common.properties.DistanceProperty;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.BaseSwerveDriveSubsystem;
import xbot.common.subsystems.drive.SwerveSimpleBezierCommand;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static edu.wpi.first.units.Units.Inches;
import static edu.wpi.first.units.Units.Meters;

public class CurveDriveToLocationCommand extends SwerveSimpleBezierCommand {
    private Pose2d target;
    private boolean allowToughTerrain;

    private final DistanceProperty safeDistance;

    private final DistanceProperty distanceToGoalAndFinish;
    private final SwervePointPathPlanning pathPlanning;

    public AtomicReference<Boolean> failed = new AtomicReference<>(false);

    @Inject
    public CurveDriveToLocationCommand(BaseSwerveDriveSubsystem drive,
            PoseSubsystem pose, PropertyFactory pf,
            HeadingModule.HeadingModuleFactory headingModuleFactory,
            RobotAssertionManager robotAssertionManager,
            SwervePointPathPlanning pathPlanning) {
        super(drive, pose, pf, headingModuleFactory, robotAssertionManager);
        pf.setPrefix("CurveDriveToLocationCommand");
        this.safeDistance = pf.createPersistentProperty("SafeDistanceInches", Inches.of(0.5));
        this.distanceToGoalAndFinish = pf.createPersistentProperty("DistanceToGoalThenFinish", Inches.of(5));
        this.pathPlanning = pathPlanning;
    }

    public CurveDriveToLocationCommand setTarget(Pose2d target) {
        this.target = target;
        return this;
    }

    public CurveDriveToLocationCommand setAllowToughTerrain(boolean allowToughTerrain) {
        this.allowToughTerrain = allowToughTerrain;
        return this;
    }

    @Override
    public void initialize() {
        log.info("Initializing");

        List<XbotSwervePoint> swervePoints =
            this.pathPlanning.generateSwervePoints(this.pose.getCurrentPose2d(), this.target, this.allowToughTerrain);
        super.logic.setKeyPoints(swervePoints);

        super.initialize();
    }
}
