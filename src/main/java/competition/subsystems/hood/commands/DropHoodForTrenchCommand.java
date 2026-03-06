package competition.subsystems.hood.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.hood.HoodSubsystem;

import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class DropHoodForTrenchCommand extends BaseCommand {
    final HoodSubsystem hood;
    final OperatorInterface oi;

    @Inject
    public DropHoodForTrenchCommand(HoodSubsystem hoodSubsystem, OperatorInterface oi) {
        this.hood = hoodSubsystem;
        this.oi = oi;
        addRequirements(hoodSubsystem);
    }

    @Override
    public void initialize() {
        hood.servoZero();
        hood.runServo();
        log.info("running servo to " + HoodSubsystem.servoMinBound);
    }

    @Override
    public void execute() {
        if (Math.abs(HoodSubsystem.servoMinBound - hood.getCurrentValue()) <= .1) {
            oi.driverGamepad.getRumbleManager().rumbleGamepad(100,100);
        }
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
