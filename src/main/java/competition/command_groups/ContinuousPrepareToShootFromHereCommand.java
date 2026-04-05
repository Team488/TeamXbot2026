package competition.command_groups;

import javax.inject.Inject;

import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.pose.TrajectoriesCalculation;
import xbot.common.command.BaseSetpointCommand;

public class ContinuousPrepareToShootFromHereCommand extends BaseSetpointCommand {

    private final HoodSubsystem hood;
    private final ShooterSubsystem shooter;
    private final PoseSubsystem poseSubsystem;
    private final TrajectoriesCalculation trajectoriesCalculation;

    public enum ShootingTarget {
        HUB,
        ALLIANCE_ZONE
    }

    private ShootingTarget target = ShootingTarget.HUB;
    private boolean zeroHood = false;

    @Inject
    public ContinuousPrepareToShootFromHereCommand(HoodSubsystem hood,
            ShooterSubsystem shooter,
            PoseSubsystem poseSubsystem,
            TrajectoriesCalculation trajectoriesCalculation) {
        super(hood, shooter);
        this.hood = hood;
        this.shooter = shooter;
        this.poseSubsystem = poseSubsystem;
        this.trajectoriesCalculation = trajectoriesCalculation;
    }

    public void setZeroHood(boolean zeroHood) {
        this.zeroHood = zeroHood;
    }

    @Override
    public void initialize() {
        this.prepareToShootAtTarget();

        super.initialize();
    }

    @Override
    public void execute() {
        this.prepareToShootAtTarget();

        super.execute();
    }

    public void setTarget(ShootingTarget shootingTarget) {
        this.target = shootingTarget;
    }

    private void prepareToShootAtTarget() {
        var pose = this.poseSubsystem.getCurrentPose2d();
        TrajectoriesCalculation.ShootingData data;
        switch (this.target) {
            case ALLIANCE_ZONE:
                data = this.trajectoriesCalculation.calculateAllianceZoneShootingDataV4(pose, this.zeroHood);
                break;
            case HUB:
            default:
                data = this.trajectoriesCalculation.calculateAllianceHubShootingData(pose, this.zeroHood);
                break;
        }

        this.shooter.setTargetValue(data.shooterRPM());
        this.hood.setTargetValue(data.servoRatio());
    }
}
