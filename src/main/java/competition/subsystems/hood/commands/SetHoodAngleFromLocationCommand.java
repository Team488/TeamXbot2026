package competition.subsystems.hood.commands;

import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.shooter.ShooterSubsystem.FieldScoringLocation;
import xbot.common.command.BaseSetpointCommand;
import javax.inject.Inject;

public class SetHoodAngleFromLocationCommand extends BaseSetpointCommand {
    private final HoodSubsystem hood;
    private FieldScoringLocation currentLocation = FieldScoringLocation.Point_1;

    @Inject
    public SetHoodAngleFromLocationCommand(HoodSubsystem hood) {
        super(hood);
        this.hood = hood;
    }

    public void setScoringLocation(FieldScoringLocation location) {
        this.currentLocation = location;
    }

    @Override
    public void initialize() {
        double angle = hood.getAngleForScoringLocation(currentLocation);
        hood.setTargetValue(angle);
    }

    @Override
    public void execute() {
        double angle = hood.getAngleForScoringLocation(currentLocation);
        hood.setTargetValue(angle);
        hood.runServo();
    }

    @Override
    public void end(boolean interrupted) {

    }

    @Override
    public boolean isFinished() {
        return hood.isMaintainerAtGoal();
    }
}
