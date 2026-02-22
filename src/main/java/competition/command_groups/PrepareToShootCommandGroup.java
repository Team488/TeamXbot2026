package competition.command_groups;

import competition.subsystems.hood.commands.HoodMaintainerCommand;
import competition.subsystems.shooter.commands.ShooterOutputCommand;
import edu.wpi.first.units.measure.AngularVelocity;
import xbot.common.command.BaseParallelCommandGroup;

import javax.inject.Inject;

public class PrepareToShootCommandGroup extends BaseParallelCommandGroup {

    HoodMaintainerCommand hoodMaintainer;
    ShooterOutputCommand outputCommand;

    public void setShooterGoal(AngularVelocity targetVelocity) {
        this.outputCommand.setTargetVelocity(targetVelocity);
    }

    public void setHoodGoal() {
        this.hoodMaintainer.
    }

    @Inject
    public PrepareToShootCommandGroup(HoodMaintainerCommand hood, ShooterOutputCommand shooterOutput) {

        this.outputCommand = shooterOutput;
        this.hoodMaintainer = hood;

        this.addCommands(shooterOutput, hood);
    }
}
