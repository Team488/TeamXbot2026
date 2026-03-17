package competition.subsystems.drive.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.DriverStation;
import javax.inject.Inject;
import xbot.common.command.BaseCommand;
import xbot.common.math.MathUtils;
import xbot.common.math.XYPair;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.Property;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.control_logic.HeadingModule;

public class RobloxDriveWithJoysticksCommand extends BaseCommand {

    OperatorInterface oi;
    DriveSubsystem drive;
    PoseSubsystem pose;
    HeadingModule headingModule;

    DoubleProperty overallDrivingPowerScale;
    DoubleProperty overallTurningPowerScale;
    DoubleProperty headingErrorTranslationScaleThreshold;
    DoubleProperty rotationPowerMultiplier;
    DoubleProperty minTranslationScaleDuringRotation;

    @Inject
    public RobloxDriveWithJoysticksCommand(
        OperatorInterface oi,
        DriveSubsystem drive,
        PoseSubsystem pose,
        PropertyFactory pf,
        HeadingModule.HeadingModuleFactory headingModuleFactory
    ) {
        pf.setPrefix(this);
        this.drive = drive;
        this.pose = pose;
        this.oi = oi;
        this.headingModule = headingModuleFactory.create(
            drive.getRotateToHeadingPid()
        );
        pf.setDefaultLevel(Property.PropertyLevel.Important);
        this.overallDrivingPowerScale = pf.createPersistentProperty(
            "DrivingPowerScale",
            1.0
        );
        this.overallTurningPowerScale = pf.createPersistentProperty(
            "TurningPowerScale",
            1.0
        );
        this.headingErrorTranslationScaleThreshold =
            pf.createPersistentProperty(
                "HeadingErrorTranslationScaleThreshold",
                45.0
            );
        this.rotationPowerMultiplier = pf.createPersistentProperty(
            "RotationPowerMultiplier",
            3
        );
        this.minTranslationScaleDuringRotation = pf.createPersistentProperty(
            "MinTranslationScaleDuringRotation",
            0.3
        );
        this.addRequirements(drive);
    }

    @Override
    public void initialize() {
        log.info("Initializing");
        drive.setDesiredHeading(pose.getCurrentHeading().getDegrees());
    }

    @Override
    public void execute() {
        // Get field-relative translation from the left joystick
        XYPair translationIntent = getRawHumanTranslationIntent();

        // Process translation (normalize, scale, precision modes)
        translationIntent = getSuggestedTranslationIntent(translationIntent);

        // Calculate desired heading from left joystick direction.
        // The left stick direction determines which way the robot faces (heading matching):
        // - Stick at 12 o'clock (forward) -> robot faces 0 degrees (far end of field)
        // - Stick at 3 o'clock (right)    -> robot faces 90 degrees (right side of field)
        // - Stick released                -> robot holds its current heading
        double leftX = MathUtils.deadband(
            oi.driverGamepad.getLeftVector().getX(),
            0.15
        );
        double leftY = MathUtils.deadband(
            oi.driverGamepad.getLeftVector().getY(),
            0.15
        );
        double desiredHeadingDegrees = getHeadingFromJoystick(leftX, leftY);

        double rotationPower;
        double headingErrorDegrees = 0;
        if (desiredHeadingDegrees < 0) {
            // Left stick is in deadzone — hold current heading, no rotation
            rotationPower = 0;
        } else {
            // Use the heading module PID to rotate toward the joystick-derived heading
            drive.setDesiredHeading(desiredHeadingDegrees);
            rotationPower = headingModule.calculateHeadingPower(
                desiredHeadingDegrees
            );

            // Apply rotation multiplier for more aggressive turning, then clamp to [-1, 1]
            rotationPower = MathUtils.constrainDouble(
                rotationPower * rotationPowerMultiplier.get(),
                -1.0,
                1.0
            );

            // Calculate heading error (shortest path, circular-aware)
            headingErrorDegrees = Math.abs(
                pose
                    .getCurrentHeading()
                    .minus(Rotation2d.fromDegrees(desiredHeadingDegrees))
                    .getDegrees()
            );
        }

        // Scale down translation when heading error is large. This prevents the robot from
        // drifting sideways when the joystick direction changes — the robot will prioritize
        // rotating to face the new direction before driving at full speed.
        // A minimum floor ensures the robot always maintains some translation while turning.
        double headingErrorThreshold =
            headingErrorTranslationScaleThreshold.get();
        double minScale = minTranslationScaleDuringRotation.get();
        if (headingErrorThreshold > 0 && headingErrorDegrees > 0) {
            // Linear ramp: full speed at 0 error, minimum floor at threshold or beyond
            double translationScale = MathUtils.constrainDouble(
                1.0 - (headingErrorDegrees / headingErrorThreshold),
                0.0,
                1.0
            );
            // Apply floor: scale ranges from minScale to 1.0 instead of 0.0 to 1.0
            translationScale = minScale + translationScale * (1.0 - minScale);
            translationIntent = translationIntent.scale(translationScale);
        }

        if (!drive.isUnlockFullDrivePowerActive()) {
            translationIntent = translationIntent.scale(
                overallDrivingPowerScale.get()
            );
            rotationPower *= overallTurningPowerScale.get();
        }

        // Drive field-relative: translation from left stick, rotation from heading PID
        drive.fieldOrientedDrive(
            translationIntent,
            rotationPower,
            pose.getCurrentHeading().getDegrees(),
            new XYPair(0, 0)
        );

        aKitLog.record("HumanTranslationIntentX", translationIntent.x);
        aKitLog.record("HumanTranslationIntentY", translationIntent.y);
        aKitLog.record("DesiredHeadingDegrees", desiredHeadingDegrees);
        aKitLog.record("HeadingErrorDegrees", headingErrorDegrees);
        aKitLog.record("RotationPower", rotationPower);
    }

    /**
     * Converts joystick X/Y into a heading angle in degrees (0-360).
     * Y-axis (up) is 0 degrees, clockwise positive.
     *
     * @param x Joystick X value (-1 to 1)
     * @param y Joystick Y value (-1 to 1)
     * @return Heading in degrees (0-360), or -1 if the joystick is in the deadzone
     */
    public static double getHeadingFromJoystick(double x, double y) {
        double magnitude = Math.sqrt(x * x + -y * -y);
        if (magnitude < 0.1) {
            return -1; // No input — signal to hold current heading
        }

        // atan2(x, y) gives angle from Y-axis (forward), clockwise positive
        double radians = Math.atan2(x, -y);
        double degrees = Math.toDegrees(radians);

        // Normalize to 0-360
        if (degrees < 0) {
            degrees += 360;
        }

        return degrees;
    }

    private XYPair getRawHumanTranslationIntent() {
        double xIntent = MathUtils.deadband(
            oi.driverGamepad.getLeftVector().getX(),
            0.15
        );
        double yIntent = MathUtils.deadband(
            oi.driverGamepad.getLeftVector().getY(),
            0.15
        );

        XYPair translationIntent = new XYPair(xIntent, yIntent);

        if (
            DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue)
            == DriverStation.Alliance.Red
        ) {
            translationIntent.rotate(180);
        }

        // We have to rotate -90 degrees to fix some alignment issues
        return translationIntent.rotate(-90);
    }

    private XYPair getSuggestedTranslationIntent(XYPair intent) {
        // Process translation: normalize & scale translationIntent, prevent diagonal movement being faster
        // This is needed even if isUnlockFullDrivePowerActive == true
        if (intent.getMagnitude() != 0) {
            double x = intent.x;
            double y = intent.y;

            // Normalize the intent
            intent = intent.scale(1 / intent.getMagnitude());

            // Scale the intent so that it reflects on how far the joystick is (assuming the values are -1 to 1)
            // (So that it will not always be the same speed as long as magnitude is > 1)
            intent = intent.scale(Math.abs(x), Math.abs(y));
        }

        if (!drive.isUnlockFullDrivePowerActive()) {
            // Scale translationIntent if precision modes active, values from XBot2024 repository
            if (drive.isExtremePrecisionTranslationActive()) {
                intent = intent.scale(0.15);
            } else if (drive.isPrecisionTranslationActive()) {
                intent = intent.scale(0.50);
            }
        }
        return intent;
    }
}
