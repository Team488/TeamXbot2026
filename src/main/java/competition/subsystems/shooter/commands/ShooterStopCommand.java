package competition.subsystems.shooter.commands;

import competition.subsystems.shooter.ShooterSubsystem;
import xbot.common.command.BaseSetpointCommand;

import javax.inject.Inject;

public class ShooterStopCommand extends BaseSetpointCommand {
    private final ShooterSubsystem subsystem;

    @Inject
    public ShooterStopCommand(ShooterSubsystem shooterSubsystem) {
        super(shooterSubsystem);
        this.subsystem = shooterSubsystem;
    }

    @Override
    public void initialize() {
        this.subsystem.setTargetVelocity(0);
    }

    @Override
    public boolean runsWhenDisabled() {
        return true;
    }
}
