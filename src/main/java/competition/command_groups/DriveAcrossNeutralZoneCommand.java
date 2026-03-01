package competition.command_groups;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import xbot.common.logging.RobotAssertionManager;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.SwervePointKinematics;
import xbot.common.subsystems.drive.SwerveSimpleBezierCommand;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryMode;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.subsystems.oracle.SwervePointPathPlanning;
import xbot.common.subsystems.pose.GameField;
import xbot.common.trajectory.XbotSwervePoint;

public class DriveAcrossNeutralZoneCommand extends SwerveSimpleBezierCommand {

    private final PoseSubsystem pose;
    private final SwervePointPathPlanning pathPlanning;
    private final GameField gameField;
    private boolean initialized = false;

    @Inject
    public DriveAcrossNeutralZoneCommand(
        DriveSubsystem drive,
        PoseSubsystem pose,
        PropertyFactory pf,
        HeadingModule.HeadingModuleFactory headingModuleFactory,
        RobotAssertionManager robotAssertionManager,
        SwervePointPathPlanning pathPlanning,
        GameField gameField
    ) {
        super(drive, pose, pf, headingModuleFactory, robotAssertionManager);
        this.gameField = gameField;
        this.pathPlanning = pathPlanning;
        this.pose = pose;
    }

    @Override
    public void initialize() {
        this.logic.setPrioritizeRotationIfCloseToGoal(true);
        this.logic.setVelocityMode(SwerveSimpleTrajectoryMode.ConstantVelocity);
        this.logic.setConstantVelocity(2);
        super.logic.setGlobalKinematicValues(
            new SwervePointKinematics(2, 1, 0, 4.5)
        );

        List<XbotSwervePoint> swervePoints = new ArrayList<XbotSwervePoint>();
        swervePoints.add(
            new XbotSwervePoint(
                new Pose2d(8.762, 5.908, Rotation2d.fromDegrees(90)),
                10
            )
        );

        this.logic.setKeyPoints(swervePoints);
        this.initialized = false;

        super.initialize();
    }

    public void execute() {
        System.out.println(isFinished());
        if (!this.initialized) {
            var currentPose = pose.getCurrentPose2d();
            System.out.println("executing");

            List<XbotSwervePoint> swervePoints =
                this.pathPlanning.generateSwervePoints(
                    currentPose,
                    new Pose2d(8.762, 5.908, Rotation2d.fromDegrees(90)),
                    false
                );

            super.logic.setKeyPoints(swervePoints);
            this.initialized = true;
        }
        super.execute();
    }
}
