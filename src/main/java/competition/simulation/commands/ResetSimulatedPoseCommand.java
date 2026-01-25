package competition.simulation.commands;

import javax.inject.Inject;

import competition.simulation.BaseSimulator;
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
        this.simulator.resetPosition(new Pose2d(3.57,.650, Rotation2d.fromRotations(Math.toRadians(0))));
        aKitLog.record("Rotation:", Rotation2d.fromDegrees(180));
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}