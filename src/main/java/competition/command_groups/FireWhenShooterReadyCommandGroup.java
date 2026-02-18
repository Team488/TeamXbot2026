package competition.command_groups;

import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.shooter.commands.ShooterOutputCommand;
import competition.subsystems.shooter_feeder.commands.ShooterFeederFire;
import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import xbot.common.command.BaseSequentialCommandGroup;

public class FireWhenShooterReadyCommandGroup extends BaseSequentialCommandGroup {

    public FireWhenShooterReadyCommandGroup(ShooterOutputCommand shootCommand,
                                            ShooterSubsystem shooterSubsystem,
                                            ShooterFeederFire shooterFeederFireCommand,
                                            HopperRollerSubsystem hopper) {
        if (shootCommand == null
                || shooterSubsystem == null
                || shooterFeederFireCommand == null
                || hopper == null) {
            return;
        }
        this.addCommands(
                hopper.getIntakeCommand(),
                shootCommand,
                shooterSubsystem.getWaitForAtGoalCommand(),
                shooterFeederFireCommand);
    }
}


