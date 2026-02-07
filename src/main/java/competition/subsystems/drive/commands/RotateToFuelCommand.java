package competition.subsystems.drive.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.drive.DriveSubsystem;
import edu.wpi.first.networktables.NetworkTableInstance;
import xbot.common.advantage.AKitLogger;
import xbot.common.command.BaseCommand;
import xbot.common.math.XYPair;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;
import java.util.Arrays;

public class RotateToFuelCommand extends BaseCommand {
    DriveSubsystem drive;
    OperatorInterface oi;
    PropertyFactory pf;
    DoubleProperty rotation;
    NetworkTableInstance networkTables = NetworkTableInstance.getDefault();

    @Inject
    public RotateToFuelCommand(
            OperatorInterface oi, DriveSubsystem drive, PropertyFactory pf
    ) {
        this.drive = drive;
        this.oi = oi;
        this.pf = pf;

        pf.setPrefix(this);
        rotation = pf.createPersistentProperty("Rotation Speed (eventually pid)", 0.05);
        this.addRequirements(drive);
    }

    @Override
    public void initialize() {
    }

    @Override
    public void execute() {
       // currently we will just hard code the indexes
        double[] boxData = networkTables
                .getTable("Vision")
                .getEntry("Boxes")
                .getDoubleArray(new double[0]);

        if (boxData.length != 0) {
            double positionX = boxData[0];
            System.out.println(Arrays.toString(boxData));

            if (positionX >= -0.3 && positionX <= 0.3) {
                drive.drive(new XYPair(0, 0), 0);
            } else {
                double appliedRotation = Math.abs(rotation.get());
                if (positionX >= 0) {
                    drive.drive(new XYPair(0, 0), appliedRotation);
                } else {
                    drive.drive(new XYPair(0, 0), -appliedRotation);
                }
            }
        } else {
            drive.drive(new XYPair(0, 0), 0);
        }
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
    }
}
