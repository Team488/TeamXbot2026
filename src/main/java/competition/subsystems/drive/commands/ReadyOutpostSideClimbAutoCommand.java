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

public class ReadyOutpostSideClimbAutoCommand extends SwerveSimpleTrajectoryCommand {

    @Inject
    public ReadyOutpostSideClimbAutoCommand(DriveSubsystem drive, PoseSubsystem pose, PropertyFactory pf,
                                            HeadingModule.HeadingModuleFactory headingModuleFactory,
                                            RobotAssertionManager robotAssertionManager,
                                            BaseSwerveDriveSubsystem baseSwerveDriveSubsystem) {
        super(drive,pose, pf, headingModuleFactory, robotAssertionManager);
        this.addRequirements(drive);
        this.drive = drive;
    }

    @Override
    public void initialize() {
        Pose2d blueClimbMiddleOutpostSideReadyPose = PoseSubsystem.convertBlueToRedIfNeeded(Landmarks.blueClimbMiddleOutpostSideReadyPose);

        ArrayList<XbotSwervePoint> swervePoints = new ArrayList<>();

        swervePoints.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint
                (blueClimbMiddleOutpostSideReadyPose,3));

        this.logic.setKeyPoints(swervePoints);
        this.logic.setConstantVelocity(drive.getMaxTargetSpeedMetersPerSecond());
        super.initialize();

    }
    @Override
    public void execute() {
        super.execute();
    }

    @Override
    public boolean isFinished() {
        return super.isFinished();
    }

    @Override
    public void end(boolean interrupted) {
        log.info("end");
    }
}