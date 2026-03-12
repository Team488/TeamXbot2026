package competition.command_groups;

import javax.inject.Inject;

import competition.subsystems.hood.commands.HoodSetCommand;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.pose.TrajectoriesCalculation;
import competition.subsystems.shooter.commands.ShooterOutputCommand;
import xbot.common.command.BaseCommand;

public class ContinuousPrepareToShootCommandGroup extends BaseCommand {

    private final HoodSetCommand hoodSetCommand;
    private final ShooterOutputCommand outputCommand;
    private final PoseSubsystem poseSubsystem;
    private final TrajectoriesCalculation trajectoriesCalculation;

    public enum ShootingTarget {
        HUB,
        ALLIANCE_ZONE
    }

    private ShootingTarget target;

    @Inject
    public ContinuousPrepareToShootCommandGroup(HoodSetCommand hoodSet,
            ShooterOutputCommand shooterOutput,
            PoseSubsystem poseSubsystem,
            TrajectoriesCalculation trajectoriesCalculation) {
        this.hoodSetCommand = hoodSet;
        this.outputCommand = shooterOutput;
        this.poseSubsystem = poseSubsystem;
        this.trajectoriesCalculation = trajectoriesCalculation;
    }

    @Override
    public void initialize() {
        this.setTargets();

        super.initialize();
    }

    @Override
    public void execute() {
        this.setTargets();

        super.execute();
    }

    public void setTarget(ShootingTarget shootingTarget) {
        this.target = shootingTarget;
    }

    private void setTargets() {
        var pose = this.poseSubsystem.getCurrentPose2d();
        TrajectoriesCalculation.ShootingData data;
        switch (this.target) {
            case ALLIANCE_ZONE:
                data = this.trajectoriesCalculation.calculateAllianceZoneShootingData(pose);
                break;
            case HUB:
            default:
                data = this.trajectoriesCalculation.calculateAllianceHubShootingData(pose);
                break;
        }

        this.outputCommand.setTargetVelocity(() -> data.shooterRPM());
        this.hoodSetCommand.setTargetRatio(() -> data.servoRatio());
    }
}
