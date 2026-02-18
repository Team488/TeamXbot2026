package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.Landmarks;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import xbot.common.logging.RobotAssertionManager;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.BaseSwerveDriveSubsystem;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import java.util.ArrayList;

public class DriveToDepotAutoCommand extends SwerveSimpleTrajectoryCommand {
    final DriveSubsystem drive;

    public Pose2d outpostPose;
    @Inject
    public DriveToDepotAutoCommand(DriveSubsystem drive, BaseSwerveDriveSubsystem baseSwerveDriveSubsystem,
                                   PropertyFactory pf, HeadingModule.HeadingModuleFactory headingModuleFactory,
                                   PoseSubsystem pose, RobotAssertionManager robotAssertionManager) {
        super(drive,pose,pf,headingModuleFactory,robotAssertionManager);
        pf.setPrefix("DepotCollection");
        this.drive = drive;
        this.addRequirements(drive);
    }

    @Override
    public void initialize() {
        Pose2d depotWallSidePose = PoseSubsystem.convertBlueToRedIfNeeded(Landmarks.blueDepotWallSide);


        ArrayList<XbotSwervePoint> swervePoints = new ArrayList<>();
        swervePoints.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                depotWallSidePose, 3));

        this.logic.setKeyPoints(swervePoints);
        this.logic.setConstantVelocity(drive.getMaxTargetSpeedMetersPerSecond());
        super.initialize();
    }

    @Override
    public void end(boolean interrupted) {
        log.info("end");
    }
}