package competition.command_groups;

import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.shooter.commands.ShooterOutputCommand;
import competition.subsystems.shooter_feeder.commands.ShooterFeederEject;
import xbot.common.command.BaseSequentialCommandGroup;

public class EjectWhenShooterReadyCommandGroup extends BaseSequentialCommandGroup {

    public EjectWhenShooterReadyCommandGroup(ShooterOutputCommand shootCommand, ShooterSubsystem shooterSubsystem, ShooterFeederEject shooterFeederEjectCommand, HopperRollerSubsystem hopper) {
        if (shootCommand == null || shooterSubsystem == null || shooterFeederEjectCommand == null || hopper == null) {
            return;
        }
        this.addCommands(
                hopper.getEjectCommand(),
                shootCommand,
                shooterSubsystem.getWaitForAtGoalCommand(),
                shooterFeederEjectCommand);
    }
}