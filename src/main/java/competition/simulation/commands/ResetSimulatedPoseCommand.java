package competition.simulation.commands;

import javax.inject.Inject;

import competition.simulation.BaseSimulator;
import competition.subsystems.pose.Landmarks;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import xbot.common.command.BaseCommand;

public class ResetSimulatedPoseCommand extends BaseCommand {
    BaseSimulator simulator;

    @Inject
    public ResetSimulatedPoseCommand(BaseSimulator simulator) {
        this.simulator = simulator;
    }

    @Override
    public void initialize() {
        this.simulator.resetPosition(Landmarks.blueStartTrenchToDepot);
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}