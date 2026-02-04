package competition.subsystems.drive.commands;

import competition.subsystems.climber.ClimberSubsystem;
import competition.subsystems.climber.commands.ClimberExtendCommand;
import competition.subsystems.climber.commands.ClimberRetractCommand;
import competition.subsystems.climber.commands.ClimberStopCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.Landmarks;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import xbot.common.command.BaseCommand;
import xbot.common.logging.RobotAssertionManager;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.BaseSwerveDriveSubsystem;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import java.util.ArrayList;

import static competition.subsystems.pose.Landmarks.blueClimbMiddleDepotSide;
import static competition.subsystems.pose.Landmarks.blueClimbMiddleDepotSideReadyPose;

public class DepotSideClimbAutoCommand extends SwerveSimpleTrajectoryCommand {

    @Inject
    public DepotSideClimbAutoCommand(DriveSubsystem drive, PoseSubsystem pose, PropertyFactory pf,
                                     HeadingModule.HeadingModuleFactory headingModuleFactory,
                                     RobotAssertionManager robotAssertionManager,
                                     BaseSwerveDriveSubsystem baseSwerveDriveSubsystem) {
        super(drive,pose, pf, headingModuleFactory, robotAssertionManager);
        this.addRequirements(drive);
        this.drive = drive;
    }

    @Override
    public void initialize() {
        Pose2d depotSideClimbPose = PoseSubsystem.convertBlueToRedIfNeeded(blueClimbMiddleDepotSide);
        Pose2d blueClimbDepotReadyPose = PoseSubsystem.convertBlueToRedIfNeeded(blueClimbMiddleDepotSideReadyPose);

        ArrayList<XbotSwervePoint> swervePoints = new ArrayList<>();

        swervePoints.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint
                (blueClimbDepotReadyPose,3));

        swervePoints.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint
                (depotSideClimbPose,4));

        this.logic.setKeyPoints(swervePoints);
        this.logic.setConstantVelocity(drive.getMaxTargetSpeedMetersPerSecond());
        super.initialize();
        log.info("DepotSideClimbAutoCommand initialized");
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

