package competition.subsystems.shooter.commands;

import competition.subsystems.shooter.ShooterSubsystem;
import xbot.common.command.BaseSetpointCommand;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.RPM;

public class ShooterStopCommand extends BaseSetpointCommand {
    private final ShooterSubsystem subsystem;

    @Inject
    public ShooterStopCommand(ShooterSubsystem shooterSubsystem) {
        super(shooterSubsystem);
        this.subsystem = shooterSubsystem;
    }

    @Override
    public void initialize() {
        this.subsystem.setTargetValue(RPM.of(0));
    }

    @Override
    public boolean runsWhenDisabled() {
        return true;
    }
}
