package competition.subsystems.shooter.commands;

import com.fasterxml.jackson.databind.ser.Serializers;
import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.shooter.ShooterSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class ShooterTestSuccess extends BaseCommand {

    public final ShooterSubsystem shooter;
    public final HoodSubsystem hood;
    public final PoseSubsystem pose;

    @Inject
    public ShooterTestSuccess (ShooterSubsystem shooterSubsystem, HoodSubsystem hoodSubsystem, PoseSubsystem poseSubsystem) {
        this.shooter = shooterSubsystem;
        this.hood = hoodSubsystem;
        this.pose = poseSubsystem;
    }

    @Override
    public void initialize() {
        log.info("Robot current position: " + pose.getCurrentPose2d());
        log.info("Shooter's RPM is: " + shooter.getTargetValue());
        log.info("Hood position is " + hood.getHoodServoRight());
        log.info("Hood position is " + hood.getHoodServoLeft());
    }
}
