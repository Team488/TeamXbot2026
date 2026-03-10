package competition.command_groups;

import competition.subsystems.hood.commands.HoodSetCommand;
import competition.subsystems.pose.TrajectoriesCalculation;
import competition.subsystems.shooter.commands.ShooterOutputCommand;
import edu.wpi.first.units.measure.AngularVelocity;
import xbot.common.command.BaseParallelCommandGroup;

import javax.inject.Inject;

public class PrepareToShootCommandGroup extends BaseParallelCommandGroup {

    private final TrajectoriesCalculation trajectoriesCalculation;
    HoodSetCommand hoodSetCommand;
    ShooterOutputCommand outputCommand;

    public PrepareToShootCommandGroup setShooterGoal(AngularVelocity targetVelocity) {
        this.outputCommand.setTargetVelocity(targetVelocity);
        return this;
    }

    public PrepareToShootCommandGroup setHoodGoal(double ratio) {
        this.hoodSetCommand.setTargetRatio(ratio);
        return this;
    }

    public PrepareToShootCommandGroup setPresetLocation(TrajectoriesCalculation.PresetShootingDistance presetShootingDistance) {
        var settings = this.trajectoriesCalculation.getPresetShootingSettings(presetShootingDistance);
        this.outputCommand.setTargetVelocity(settings.shooterRPM());
        this.hoodSetCommand.setTargetRatio(settings.hoodServoRatio());
        return this;
    }

    @Inject
    public PrepareToShootCommandGroup(HoodSetCommand hoodSet, ShooterOutputCommand shooterOutput, TrajectoriesCalculation trajectoriesCalculation) {
        this.trajectoriesCalculation = trajectoriesCalculation;
        this.outputCommand = shooterOutput;
        this.hoodSetCommand = hoodSet;

        this.addCommands(shooterOutput, hoodSet);
    }
}
