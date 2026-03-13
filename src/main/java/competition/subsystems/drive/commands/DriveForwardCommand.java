package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Translation2d;
import xbot.common.command.BaseCommand;
import xbot.common.logging.RobotAssertionManager;
import xbot.common.math.XYPair;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;
import competition.subsystems.pose.PoseSubsystem;
import xbot.common.subsystems.drive.BaseSwerveDriveSubsystem;
import xbot.common.subsystems.drive.SwerveSimpleTrajectoryCommand;
import xbot.common.subsystems.drive.control_logic.HeadingModule;


import javax.inject.Inject;

import static edu.wpi.first.units.Units.RPM;

public class DriveForwardCommand extends SwerveSimpleTrajectoryCommand {

    public PoseSubsystem pose;
    public final DriveSubsystem drive;
    public final DoubleProperty power;
    public Transform2d transform2d;
    public double target = 0;

    @Inject
    public DriveForwardCommand(DriveSubsystem drive, BaseSwerveDriveSubsystem baseSwerveDriveSubsystem,
                               PropertyFactory pf, HeadingModule.HeadingModuleFactory headingModuleFactory,
                               PoseSubsystem pose, RobotAssertionManager robotAssertionManager)
    {
        super(drive,pose,pf,headingModuleFactory,robotAssertionManager);
        this.drive = drive;
        this.pose = pose;
        this.power = pf.createPersistentProperty("PowerForward", 1);
        addRequirements(drive);
    }

    @Override
    public void initialize() {
        double currentHeading = pose.getCurrentHeading().getDegrees();
    }

    public double currentHeading() {
    }

    public void setTarget(double d) {
        target = d; //called in operatorCommandMap with 2
    }

    public boolean reachedTarget() {
        double error = Math.abs(currentHeading()-target);
        return error < 2;
    }

        public void execute() {
            double error = target - currentHeading();
            drive.move(new XYPair(power.get(), 0), 0);
    }

    public boolean isFinished() {
        return reachedTarget();
    }

    public void end(boolean interrupted) {
        if (interrupted) {
            drive.stop();
        }
    }
}
