package competition.subsystems.drive.commands;

import competition.subsystems.drive.DriveSubsystem;
import edu.wpi.first.units.measure.AngularVelocity;
import xbot.common.command.BaseCommand;
import xbot.common.math.XYPair;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.RPM;

public class DriveForwardCommand extends BaseCommand {

    public final DriveSubsystem drive;
    public final DoubleProperty power;

    @Inject
    public DriveForwardCommand(DriveSubsystem driveSubsystem, PropertyFactory propertyFactory) {
        this.drive = driveSubsystem;
        this.power = propertyFactory.createPersistentProperty("PowerForward", 1);
        addRequirements(driveSubsystem);
    }

    @Override
    public void initialize() {
        super.initialize();
        drive.move(new XYPair(power.get(), 0), 0);
    }
}
