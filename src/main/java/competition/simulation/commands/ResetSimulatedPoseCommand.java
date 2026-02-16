package competition.simulation.commands;

import javax.inject.Inject;

import competition.simulation.BaseSimulator;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import xbot.common.command.BaseCommand;

public class ResetSimulatedPoseCommand extends BaseCommand {
    BaseSimulator simulator;
    Pose2d startPose;

    @Inject
    public ResetSimulatedPoseCommand(BaseSimulator simulator, Pose2d startPose) {
        this.simulator = simulator;
        this.startPose = startPose;
    }

    @Override
    public void initialize() {
        this.simulator.resetPosition(startPose);
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}