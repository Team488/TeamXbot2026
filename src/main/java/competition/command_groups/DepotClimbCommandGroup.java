    package competition.command_groups;

    import competition.subsystems.climber.commands.ClimberExtendCommand;
    import competition.subsystems.climber.commands.ClimberRetractCommand;
    import competition.subsystems.drive.DriveSubsystem;
    import competition.subsystems.intake_deploy.commands.IntakeDeployRetractCommand;
    import competition.subsystems.pose.PoseSubsystem;
    import edu.wpi.first.math.geometry.Pose2d;
    import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
    import xbot.common.command.BaseSequentialCommandGroup;
    import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
    import xbot.common.trajectory.XbotSwervePoint;

    import javax.inject.Inject;
    import javax.inject.Provider;

    import java.util.ArrayList;

    import static competition.subsystems.pose.Landmarks.blueDepotSideClimbReadyPose;
    import static competition.subsystems.pose.Landmarks.blueClimbMiddleDepotSide;

    public class DepotClimbCommandGroup extends BaseSequentialCommandGroup {

        @Inject
        public DepotClimbCommandGroup(Provider<SwerveSimpleTrajectoryCommand> trajectoryProvider,
                                      DriveSubsystem drive,
                                      ClimberExtendCommand climberExtendCommand,
                                      ClimberRetractCommand climberRetractCommand,
                                      IntakeDeployRetractCommand intakeDeployRetractCommand) {
            SwerveSimpleTrajectoryCommand readyDepotClimb =  trajectoryProvider.get();
            SwerveSimpleTrajectoryCommand depotSideClimb = trajectoryProvider.get();

            ArrayList<XbotSwervePoint> readyDepotPoint = new ArrayList<>();
            ArrayList<XbotSwervePoint> climbPoint = new ArrayList<>();

            Pose2d readyDepotSideClimbPose =
                    PoseSubsystem.convertBlueToRedIfNeeded(blueDepotSideClimbReadyPose);

            Pose2d depotSideClimbPose =
                    PoseSubsystem.convertBlueToRedIfNeeded(blueClimbMiddleDepotSide);

            readyDepotPoint.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint
                    (readyDepotSideClimbPose,4));

            climbPoint.add(XbotSwervePoint.createPotentiallyFilppedXbotSwervePoint
                    (depotSideClimbPose,4));

            readyDepotClimb.logic.setKeyPoints(readyDepotPoint);
            readyDepotClimb.logic.setConstantVelocity(drive.getMaxTargetSpeedMetersPerSecond());

            depotSideClimb.logic.setKeyPoints(climbPoint);
            depotSideClimb.logic.setConstantVelocity(drive.getMaxTargetSpeedMetersPerSecond());

            addCommands(
                    readyDepotClimb,
                    new ParallelCommandGroup(
                            intakeDeployRetractCommand,
                            climberExtendCommand
                    ),
                    depotSideClimb,
                    climberRetractCommand
            );
        }
    }
