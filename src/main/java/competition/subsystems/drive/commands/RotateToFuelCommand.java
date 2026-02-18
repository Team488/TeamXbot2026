package competition.subsystems.drive.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.drive.DriveSubsystem;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.networktables.NetworkTableInstance;
import org.photonvision.PhotonCamera;
import xbot.common.advantage.AKitLogger;
import xbot.common.command.BaseCommand;
import xbot.common.math.XYPair;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;

import javax.inject.Inject;

public class RotateToFuelCommand extends BaseCommand {
    DriveSubsystem drive;
    OperatorInterface oi;
    PropertyFactory pf;
    DoubleProperty rotation;
    PhotonCamera camera;

    final DoubleProperty turnP;
    final DoubleProperty turnI;
    final DoubleProperty turnD;
    final PIDController turnController;

    @Inject
    public RotateToFuelCommand(
            OperatorInterface oi, DriveSubsystem drive, PropertyFactory pf
    ) {
        this.drive = drive;
        this.oi = oi;
        this.pf = pf;
        this.camera = new PhotonCamera(NetworkTableInstance.getDefault(), "color_camera_ov9782");

        pf.setPrefix(this);
        rotation = pf.createPersistentProperty("Rotation Speed (eventually pid)", 0.05);
        this.addRequirements(drive);

        turnP = pf.createPersistentProperty("Rotate To Fuel P", 0.6);
        turnI = pf.createPersistentProperty("Rotate To Fuel I", 0.0);
        turnD = pf.createPersistentProperty("Rotate To Fuel D", 0.12);
        turnController = new PIDController(turnP.get(), turnI.get(), turnD.get());
    }

    @Override
    public void initialize() {
    }

    @Override
    public void execute() {
        turnController.setP(turnP.get());
        turnController.setI(turnI.get());
        turnController.setD(turnD.get());
        // 1. Define this in your Constants or Subsystem constructor
        // 1. Get the list of all results since the last loop
        var results = camera.getAllUnreadResults();

        double rotationValue = 0;

        // 2. Check if the list isn't empty
        if (!results.isEmpty()) {
            // 3. Grab the most recent result (the last one in the list)
            var latestResult = results.get(results.size() - 1);

            if (latestResult.hasTargets()) {
                double currentYaw = latestResult.getBestTarget().getYaw();

                // Use your PIDController to calculate smoothness
                rotationValue = turnController.calculate(currentYaw, 0);
            }
        } else {
            rotationValue = 0;
        }

        drive.drive(new XYPair(0, 0), rotationValue);
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
    }
}
