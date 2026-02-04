package competition.subsystems.drive.commands;

import competition.subsystems.climber.ClimberSubsystem;
import competition.subsystems.climber.commands.ClimberExtendCommand;
import competition.subsystems.climber.commands.ClimberRetractCommand;
import competition.subsystems.climber.commands.ClimberStopCommand;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.Landmarks;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import xbot.common.command.BaseCommand;
import xbot.common.logging.RobotAssertionManager;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.BaseSwerveDriveSubsystem;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.trajectory.XbotSwervePoint;

import javax.inject.Inject;
import java.util.ArrayList;

    public class OutpostSideClimbAutoCommand extends SwerveSimpleTrajectoryCommand {


        @Inject
        public OutpostSideClimbAutoCommand(DriveSubsystem drive, PoseSubsystem pose, PropertyFactory pf,
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
            Pose2d supportPoint = PoseSubsystem.convertBlueToRedIfNeeded(
                    new Pose2d(2.136,3.630, blueClimbMiddleOutpostSideReadyPose.getRotation()));
            Pose2d blueClimbMiddleOutpostSide = PoseSubsystem.convertBlueToRedIfNeeded(Landmarks.blueClimbMiddleOutpostSide);


            ArrayList<XbotSwervePoint> swervePoints = new ArrayList<>();

            swervePoints.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint(
                    supportPoint, 3));


            swervePoints.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint
                    (blueClimbMiddleOutpostSideReadyPose,2));

            swervePoints.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint
                    (blueClimbMiddleOutpostSide,2));


            this.logic.setKeyPoints(swervePoints);
//            this.logic.setConstantVelocity(drive.getMaxTargetSpeedMetersPerSecond());
            super.initialize();
            log.info("Outpost Side Climb Auto Command initialized");

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