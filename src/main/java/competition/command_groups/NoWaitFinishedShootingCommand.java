package competition.command_groups;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.RPM;
import competition.subsystems.collector_intake.commands.CollectorIntakeCommand;
import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.hopper_roller.HopperRollerSubsystem;
import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.shooter.commands.ShooterOutputCommand;
import competition.subsystems.shooter_feeder.commands.ShooterFeederFire;
import xbot.common.command.BaseParallelCommandGroup;
import xbot.common.command.NamedInstantCommand;

public class NoWaitFinishedShootingCommand extends BaseParallelCommandGroup {

    @Inject
    public NoWaitFinishedShootingCommand(HopperRollerSubsystem hopper,
            ShooterSubsystem shooterSubsystem,
            HoodSubsystem hoodSubsystem,
            ShooterOutputCommand shooterOutputCommand,
            ShooterFeederFire shooterFeederFireCommand,
            CollectorIntakeCommand fuelIntakeCommand) {
        var setHoodCommand = new NamedInstantCommand("Set Hood Min", () -> hoodSubsystem.setTargetValue(0.0));
        var setShooterGoal = new NamedInstantCommand("Set Shooter Stop", () -> shooterSubsystem.setPower(0.0));
        var setHopperRoller = new NamedInstantCommand("Set Hopper Roller Stop", () -> hopper.stop());

        this.addCommands(setHoodCommand, setShooterGoal, setHopperRoller);
    }
}
