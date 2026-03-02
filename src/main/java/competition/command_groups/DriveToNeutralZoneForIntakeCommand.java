package competition.command_groups;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.path.PathConstraints;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.units.Units;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj2.command.Command;
import xbot.common.subsystems.pose.GameField;

import javax.inject.Inject;

public class DriveToNeutralZoneForIntakeCommand extends Command {
    private final PoseSubsystem pose;
    private final GameField gameField;

    private Command pathfindingCommand;

    @Inject
    public DriveToNeutralZoneForIntakeCommand(DriveSubsystem drive, PoseSubsystem pose, GameField gameField) {
        this.pose = pose;
        this.gameField = gameField;
        addRequirements(drive);
    }

    @Override
    public void initialize() {
        // Compute the finalPoint — same transform logic as the original code.
        // This is the point across the neutral zone into the fuel field.
        Pose2d closestTrench = this.pose.closestAllianceTrench();
        var fieldCenter = this.gameField.getFieldCenter();
        var changeInX = DriverStation.getAlliance().orElse(Alliance.Blue) == Alliance.Blue ? -1 : 1;
        var changeInY = closestTrench.getY() > fieldCenter.getY() ? -1 : 1;
        var finalTransform = new Transform2d(Units.Meters.of(3 * -1 * changeInX), Units.Meters.of(1 * changeInY),
                changeInX * changeInY == 1 ? Rotation2d.kCCW_Pi_2 : Rotation2d.kCW_Pi_2);
        var finalPoint = closestTrench.plus(finalTransform);

        // PathPlanner's AD* algorithm with the navgrid handles obstacle avoidance.
        PathConstraints constraints = new PathConstraints(
                3.0,   // max velocity m/s
                3.0,   // max acceleration m/s^2
                Math.toRadians(540),  // max angular velocity rad/s
                Math.toRadians(720)   // max angular acceleration rad/s^2
        );

        pathfindingCommand = AutoBuilder.pathfindToPose(finalPoint, constraints);
        pathfindingCommand.initialize();

//
//        Pose2d closestTrench = this.pose.closestAllianceTrench();
//        var changeInX = DriverStation.getAlliance().orElse(Alliance.Blue) == Alliance.Blue ? -1 : 1;
//        var neutralSideTransform = new Transform2d(Units.Meters.of(2 * -1 * changeInX), Units.Meters.of(0),
//                Rotation2d.kZero);
//
//        var neutralPoint = closestTrench.plus(neutralSideTransform);
//
//        // PathPlanner's AD* algorithm with the navgrid handles obstacle avoidance.
//        // Pathfind to the neutral-side point of the trench (the entry into the neutral zone).
//        // The second command (DriveAcrossNeutralZoneCommand) will continue to finalPoint.
//        PathConstraints constraints = new PathConstraints(
//                3.0,   // max velocity m/s
//                3.0,   // max acceleration m/s^2
//                Math.toRadians(540),  // max angular velocity rad/s
//                Math.toRadians(720)   // max angular acceleration rad/s^2
//        );
//
//        pathfindingCommand = AutoBuilder.pathfindToPose(neutralPoint, constraints);
//        pathfindingCommand.initialize();
    }

    @Override
    public void execute() {
        if (pathfindingCommand != null) {
            pathfindingCommand.execute();
        }
    }

    @Override
    public boolean isFinished() {
        if (pathfindingCommand != null) {
            return pathfindingCommand.isFinished();
        }
        return true;
    }

    @Override
    public void end(boolean interrupted) {
        if (pathfindingCommand != null) {
            pathfindingCommand.end(interrupted);
        }
    }
}
