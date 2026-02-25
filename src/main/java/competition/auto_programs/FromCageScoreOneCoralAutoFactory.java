package competition.auto_programs;

import competition.commandgroups.DriveToFaceAndScoreCommandGroupFactory;
import competition.commandgroups.PrepCoralSystemCommandGroupFactory;
import competition.simulation.BaseSimulator;
import competition.subsystems.pose.Landmarks;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

import javax.inject.Inject;

public class FromCageScoreOneCoralAutoFactory {

    final AutonomousCommandSelector autoSelector;
    BaseSimulator simulator;
    PoseSubsystem pose;
    DriveToFaceAndScoreCommandGroupFactory driveToFaceAndScoreCommandGroupFact;
    PrepCoralSystemCommandGroupFactory prepCoralSystemCommandGroupFact;

    @Inject
    public FromCageScoreOneCoralAutoFactory(AutonomousCommandSelector autoSelector,
                                            BaseSimulator simulator,
                                            PoseSubsystem pose,
                                            DriveToFaceAndScoreCommandGroupFactory driveToFaceAndScoreCommandGroupFact,
                                            PrepCoralSystemCommandGroupFactory prepCoralSystemCommandGroupFact) {
        this.autoSelector = autoSelector;
        this.simulator = simulator;
        this.pose = pose;
        this.driveToFaceAndScoreCommandGroupFact = driveToFaceAndScoreCommandGroupFact;
        this.prepCoralSystemCommandGroupFact = prepCoralSystemCommandGroupFact;
    }

    public BaseAutonomousSequentialCommandGroup create(Pose2d startingLocation,
                                                       Landmarks.ReefFace targetReefFace, Landmarks.Branch targetBranch,
                                                       Landmarks.CoralLevel targetLevel) {
        var auto = new BaseAutonomousSequentialCommandGroup(autoSelector);
        auto.setName("FromCageScoreOneCoralAuto");

        var initializeStateCommand = pose.createSetPositionCommand(
                        () -> PoseSubsystem.convertBlueToRedIfNeeded(startingLocation)
                )
                .alongWith(new InstantCommand(() -> simulator.resetPosition(PoseSubsystem.convertBlueToRedIfNeeded(startingLocation))));
        auto.addCommands(initializeStateCommand);

        var driveAndScore = driveToFaceAndScoreCommandGroupFact.create(targetReefFace, targetBranch, targetLevel)
                .alongWith(
                        auto.getDriveAndScoreStatusMessageCommand(targetReefFace, targetBranch, targetLevel));
        auto.addCommands(driveAndScore);

        var homeCoralSystem = prepCoralSystemCommandGroupFact.create(() -> Landmarks.CoralLevel.CORAL_COLLECTING);
        auto.addCommands(homeCoralSystem);

        return auto;
    }
}
