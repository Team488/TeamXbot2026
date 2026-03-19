package competition.subsystems.shooter.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.shooter.ShooterSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class WhenShooterReadyRumbleCommand extends BaseCommand {

    final HoodSubsystem hood;
    final OperatorInterface oi;
    final ShooterSubsystem shooter;


    @Inject
    public WhenShooterReadyRumbleCommand(HoodSubsystem hoodSubsystem, OperatorInterface oi, ShooterSubsystem shooterSubsystem) {
        this.hood = hoodSubsystem;
        this.oi = oi;
        this.shooter = shooterSubsystem;
    }

    @Override
    public void execute() {
        if (shooter.isReadyToFire() && hood.isMaintainerAtGoal()){
            oi.driverGamepad.getRumbleManager().rumbleGamepad(150, 3);
        } else {
            oi.driverGamepad.getRumbleManager().stopGamepadRumble();
        }
    }

    @Override
    public void end(boolean isInterrupted) {
        super.end(isInterrupted);
        oi.driverGamepad.getRumbleManager().stopGamepadRumble();
    }

    @Override
    public boolean runsWhenDisabled() {
        return true;
    }
}
