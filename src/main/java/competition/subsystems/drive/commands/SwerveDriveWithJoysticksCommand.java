package competition.subsystems.drive.commands;

import competition.operator_interface.OperatorInterface;
import competition.subsystems.drive.DriveSubsystem;
import edu.wpi.first.wpilibj.DriverStation;
import xbot.common.logic.HumanVsMachineDecider;
import xbot.common.logic.HumanVsMachineDecider.HumanVsMachineDeciderFactory;
import xbot.common.subsystems.drive.swerve.SwerveSuggestedRotation;
import xbot.common.subsystems.drive.swerve.SwerveDriveRotationAdvisor;
import competition.subsystems.pose.PoseSubsystem;
import xbot.common.command.BaseCommand;
import xbot.common.math.MathUtils;
import xbot.common.math.XYPair;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.Property;
import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.drive.control_logic.HeadingModule;
import xbot.common.subsystems.drive.control_logic.HeadingModule.HeadingModuleFactory;

import javax.inject.Inject;

public class SwerveDriveWithJoysticksCommand extends BaseCommand {

    OperatorInterface oi;
    DriveSubsystem drive;
    PoseSubsystem pose;
    HeadingModule headingModule;

    DoubleProperty overallDrivingPowerScale;
    DoubleProperty overallTurningPowerScale;

    SwerveDriveRotationAdvisor advisor;
    HumanVsMachineDecider hvmDecider;

    @Inject
    public SwerveDriveWithJoysticksCommand(
            OperatorInterface oi, DriveSubsystem drive, PoseSubsystem pose, PropertyFactory pf,
            HeadingModuleFactory headingModuleFactory, HumanVsMachineDeciderFactory hvmFactory) {
        pf.setPrefix(this);
        this.drive = drive;
        this.pose = pose;
        this.oi = oi;
        this.headingModule = headingModuleFactory.create(drive.getRotateToHeadingPid());
        this.hvmDecider = hvmFactory.create(pf.getPrefix());
        this.advisor = new SwerveDriveRotationAdvisor(pose, drive, pf, hvmDecider);
        pf.setDefaultLevel(Property.PropertyLevel.Important);
        this.overallDrivingPowerScale = pf.createPersistentProperty("DrivingPowerScale", 1.0);
        this.overallTurningPowerScale = pf.createPersistentProperty("TurningPowerScale", 1.0);
        this.addRequirements(drive);
    }

    @Override
    public void initialize() {
        log.info("Initializing");
        advisor.resetDecider();
        drive.setDesiredHeading(pose.getCurrentHeading().getDegrees());
    }

    @Override
    public void execute() {
        // Get raw human translate and rotation intents
        XYPair translationIntent = getRawHumanTranslationIntent();
        double rawRotationIntent = getRawHumanRotationIntent();

        // Process the translation intent
        translationIntent = getSuggestedTranslationIntent(translationIntent);

        // Checks snapping to side or other rotation features to get suggested intent
        double rotationIntent = getSuggestedRotationIntent(rawRotationIntent);

        if (!drive.isUnlockFullDrivePowerActive()) {
            translationIntent = translationIntent.scale(overallDrivingPowerScale.get());
            rotationIntent *= overallTurningPowerScale.get();
        }

        // Field oriented drive will process the actual swerve movements for us
        drive.fieldOrientedDrive(
                translationIntent,
                rotationIntent,
                pose.getCurrentHeading().getDegrees(),
                new XYPair(0,0)
        );
    }

    private XYPair getRawHumanTranslationIntent() {
        double xIntent = MathUtils.deadband(oi.gamepad.getLeftVector().getX(), 0.15);
        double yIntent = MathUtils.deadband(oi.gamepad.getLeftVector().getY(), 0.15);

        XYPair translationIntent = new XYPair(xIntent, yIntent);

        if (DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue) == DriverStation.Alliance.Red) {
            translationIntent.rotate(180);
        }

        // We have to rotate -90 degrees to fix some alignment issues
        return translationIntent.rotate(-90);
    }

    private double getRawHumanRotationIntent() {
        // Deadband is to prevent buggy joysticks/triggers
        double rotateLeftIntent = MathUtils.deadband(oi.gamepad.getLeftTrigger(), 0.05);
        double rotateRightIntent = MathUtils.deadband(oi.gamepad.getRightTrigger(), 0.05);

        // Merge the two trigger values together in case of conflicts
        // Rotate left = positive, right = negative
        return rotateLeftIntent - rotateRightIntent;
    }

    public double getSuggestedRotationIntent(double triggerRotateIntent) {
        // Checks the right joystick input to see if we want to snap to a certain side
        // Apparently, we need to invert the x input here as it has been inverted for other commands already
        // And of course, we must rotate -90 (similar to how we got raw translation) for default alignment
        XYPair joystickInput = new XYPair(-oi.gamepad.getRightVector().getX(), oi.gamepad.getRightVector().getY()).rotate(-90);

        SwerveSuggestedRotation suggested = advisor.getSuggestedRotationValue(joystickInput, triggerRotateIntent);
        return processSuggestedRotationValueIntoPower(suggested);
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

    private double processSuggestedRotationValueIntoPower(SwerveSuggestedRotation suggested) {
        return switch (suggested.type) {
            case DesiredHeading -> {
                yield headingModule.calculateHeadingPower(suggested.value);
            }
            case HumanControlHeadingPower -> {
                if (drive.isPrecisionRotationActive()) {
                    yield suggested.value *= 0.25;
                }
                yield suggested.value;
            }
        };
    }
}