package competition.general_commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.hood.HoodSubsystem;
import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.vision.AprilTagVisionSubsystemExtended;
import edu.wpi.first.wpilibj.DriverStation;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

public class GamepadRumbleCommand extends BaseCommand {

    final HoodSubsystem hood;
    final OperatorInterface oi;
    final ShooterSubsystem shooter;
    final DriveSubsystem drive;
    final AprilTagVisionSubsystemExtended vision;


    @Inject
    public GamepadRumbleCommand(HoodSubsystem hoodSubsystem, OperatorInterface oi, ShooterSubsystem shooterSubsystem,
                                DriveSubsystem drive, AprilTagVisionSubsystemExtended vision) {
        this.hood = hoodSubsystem;
        this.oi = oi;
        this.shooter = shooterSubsystem;
        this.drive = drive;
        this.vision = vision;
    }

    @Override
    public void execute() {
        var lookAtPointFault = drive.getLookAtPointActive() && !vision.areAllCamerasConnected();
        var shooterReady = DriverStation.isTeleop() && shooter.isReadyToFire() && hood.isMaintainerAtGoal();

        if (shooterReady) {
            oi.driverGamepad.getRumbleManager().rumbleGamepad(0.5, 0.2);
            oi.operatorGamepad.getRumbleManager().rumbleGamepad(0.5, 0.2);
        }

        if (lookAtPointFault) {
            // Override the driver gamepad rumble to indicate fault
            oi.driverGamepad.getRumbleManager().rumbleGamepad(1, 0.5);
        }
    }

    @Override
    public void end(boolean isInterrupted) {
        super.end(isInterrupted);
        oi.driverGamepad.getRumbleManager().stopGamepadRumble();
        oi.operatorGamepad.getRumbleManager().stopGamepadRumble();
    }

    @Override
    public boolean runsWhenDisabled() {
        return true;
    }
}
