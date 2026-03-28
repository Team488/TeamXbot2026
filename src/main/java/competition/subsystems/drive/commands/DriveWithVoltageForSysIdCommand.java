package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import xbot.common.command.BaseCommand;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.Volts;

public class DriveWithVoltageForSysIdCommand extends BaseCommand {
    private final DriveSubsystem drive;

    @Inject
    public DriveWithVoltageForSysIdCommand(DriveSubsystem drive) {
        this.drive = drive;

        this.addRequirements(
                drive.getFrontLeftSwerveModuleSubsystem().getDriveSubsystem(),
                drive.getFrontRightSwerveModuleSubsystem().getDriveSubsystem(),
                drive.getRearLeftSwerveModuleSubsystem().getDriveSubsystem(),
                drive.getRearRightSwerveModuleSubsystem().getDriveSubsystem()
        );
    }

    @Override
    public void initialize() {
        drive.forEachSwerveModule(module -> {
            module.getSteeringSubsystem().setTargetValue(0.0);
            module.getDriveSubsystem().setPower(0.0);
        });
    }

    @Override
    public void execute() {
        var targetPower = drive.voltageDriveLevel.in(Volts) / 12.0;

        drive.forEachSwerveModule(module -> {
            module.getDriveSubsystem().setPower(targetPower);
        });
    }

    @Override
    public void end(boolean isInterrupted) {
        drive.forEachSwerveModule(module -> {
            module.getDriveSubsystem().setPower(0.0);
        });
    }
}
