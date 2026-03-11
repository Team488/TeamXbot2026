package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
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
        drive.move(new XYPair(power.get(), 0), 0);
        double currentHeading = pose.getCurrentHeading().getDegrees(); //get heading and set target
        // get current heading, error, speed, power
        //if (target is out of bounds)
        //calibrate/set it back to original heading

    }

    public double currentHeading() {
        return pose.getCurrentHeading().getDegrees(); //get heading and set target
    }

    public void setTarget(double d) {
        target = d; //called in operatorCommandMap with 2
    }

    public boolean reachedTarget() {
        
    }

        public void execute() {
        double error = currentHeading().minus(target);
    }

    public boolean isFinished() {
        if (reachedTarget()) {
            end();
        }
        //double error = pose.getCurrentHeading().getDegrees() - goal;
        //double speed = pose.getCurrentHeading().getDegrees() - previousPos;

        //return Math.abs(error) < 0.05 && Math.abs(speed) < 0.05;

        //add it finishes when reached to a specific distance
    }

    public void end() {
        power.set(0.0);
    }
}
