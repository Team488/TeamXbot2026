package competition.command_groups;

import competition.subsystems.hood.commands.HoodSetCommand;
import competition.subsystems.shooter.commands.ShooterOutputCommand;
import edu.wpi.first.units.measure.AngularVelocity;
import xbot.common.command.BaseParallelCommandGroup;

import java.util.function.Supplier;

import javax.inject.Inject;

public class PrepareToShootCommandGroup extends BaseParallelCommandGroup {

    HoodSetCommand hoodSetCommand;
    ShooterOutputCommand outputCommand;

    public PrepareToShootCommandGroup setShooterGoal(AngularVelocity targetVelocity) {
        this.outputCommand.setTargetVelocity(targetVelocity);
        return this;
    }

    public PrepareToShootCommandGroup setShooterGoal(Supplier<AngularVelocity> targetVelocitySupplier) {
        this.outputCommand.setTargetVelocity(targetVelocitySupplier);
        return this;
    }

    public PrepareToShootCommandGroup setHoodGoal(double ratio) {
        this.hoodSetCommand.setTargetRatio(ratio);
        return this;
    }

    public PrepareToShootCommandGroup setHoodGoal(Supplier<Double> targetRatioSupplier) {
        this.hoodSetCommand.setTargetRatio(targetRatioSupplier);
        return this;
    }

    @Inject
    public PrepareToShootCommandGroup(HoodSetCommand hoodSet, ShooterOutputCommand shooterOutput) {

        this.outputCommand = shooterOutput;
        this.hoodSetCommand = hoodSet;

        this.addCommands(shooterOutput, hoodSet);
    }
}