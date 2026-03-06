package competition.command_groups;

import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.hood.commands.HoodSetCommand;
import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.shooter.commands.ShooterOutputCommand;
import edu.wpi.first.units.measure.AngularVelocity;
import xbot.common.command.BaseParallelCommandGroup;

import javax.inject.Inject;

public class PrepareToShootCommandGroup extends BaseParallelCommandGroup {

    HoodSetCommand hoodSetCommand;
    ShooterOutputCommand outputCommand;

    public void setShooterGoal(AngularVelocity targetVelocity) {
        this.outputCommand.setTargetVelocity(targetVelocity);
    }

    public void setHoodGoal(double ratio) {
        this.hoodSetCommand.setTargetRatio(ratio);
    }

    @Inject
    public PrepareToShootCommandGroup(HoodSubsystem hood, HoodSetCommand hoodSet, ShooterSubsystem shooter,
                                      ShooterOutputCommand shooterOutput) {

        var hoodWaitCommand = hood.getWaitForAtGoalCommand();
        var shooterWaitCommand = shooter.getWaitForAtGoalCommand();

        this.outputCommand = shooterOutput;
        this.hoodSetCommand = hoodSet;

        this.addCommands(shooterOutput, shooterWaitCommand, hoodSet, hoodWaitCommand);
    }
}
