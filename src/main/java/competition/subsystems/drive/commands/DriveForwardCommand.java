package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import edu.wpi.first.units.measure.AngularVelocity;
import xbot.common.command.BaseCommand;
import xbot.common.math.XYPair;
import xbot.common.properties.DoubleProperty;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.RPM;

public class DriveForwardCommand extends BaseCommand {

    private final DriveSubsystem drive;
    //private AngularVelocity targetVelocity;
    //private boolean usingCustomGoal = false;
    public DoubleProperty power;

    @Inject
    public DriveForwardCommand(DriveSubsystem driveSubsystem) {
        this.drive = driveSubsystem;
        addRequirements(driveSubsystem);
    }


    //public void setTargetVelocity(AngularVelocity targetVelocity) {
    //    this.targetVelocity = targetVelocity;
    //    this.usingCustomGoal = true;
    //}

    @Override
    public void initialize() {
        drive.move(new XYPair(power.get(),0),0);
        //if (!this.usingCustomGoal) {
    //        this.targetVelocity = RPM.of(this.drive.);
    //    }

    //    log.info("Drive Meter: ", this.targetVelocity.in(RPM));
    }

    @Override
    public boolean isFinished () {
        return false;
    }
}
