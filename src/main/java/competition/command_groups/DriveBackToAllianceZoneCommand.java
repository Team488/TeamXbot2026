package competition.command_groups;

import competition.electrical_contract.ElectricalContract;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.Landmarks;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.units.Units;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.wpilibj.DriverStation;
import xbot.common.logging.RobotAssertionManager;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.SwerveSimpleBezierCommand;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.subsystems.pose.GameField;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class DriveBackToAllianceZoneCommand extends SwerveSimpleBezierCommand {
    private final PoseSubsystem pose;
    private final GameField gameField;
    private final Distance robotRadius;

    @Inject
    public DriveBackToAllianceZoneCommand(DriveSubsystem drive, PoseSubsystem pose,
                                          PropertyFactory pf, HeadingModule.HeadingModuleFactory headingModuleFactory,
                                          RobotAssertionManager robotAssertionManager, GameField gameField, ElectricalContract electricalContract) {
        super(drive, pose, pf, headingModuleFactory, robotAssertionManager);
        this.gameField = gameField;
        this.pose = pose;
        this.robotRadius = electricalContract.getRadiusOfRobot();
    }

    @Override
    public void initialize() {
        List<XbotSwervePoint> points = new ArrayList<>();

        var currentPose = pose.getCurrentPose2d();

        var ballPitEdge = Landmarks.getFurthestAutoBallPitEdge(this.gameField, currentPose, DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue));
        var multiplier = ballPitEdge.getY() > this.gameField.getFieldCenter().getY() ? 1 : -1;
        var adjustedForRobot = new Translation2d(Units.Meters.of(0),
                this.robotRadius.times(multiplier));

        var across = new Pose2d(ballPitEdge.getTranslation().plus(adjustedForRobot), ballPitEdge.getRotation().minus(Rotation2d.fromDegrees(90)));

        points.add(new XbotSwervePoint(across, 3));

        var closest = pose.furthestAllianceTrench();

        points.add(new XbotSwervePoint(closest, 0.5));

        super.logic.setKeyPoints(points);

        super.initialize();
    }
}
