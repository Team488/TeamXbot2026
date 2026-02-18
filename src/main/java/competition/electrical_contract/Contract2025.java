package competition.electrical_contract;

import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;
import xbot.common.injection.electrical_contract.CANBusId;
import xbot.common.injection.electrical_contract.CANMotorControllerInfo;
import xbot.common.injection.electrical_contract.CANMotorControllerOutputConfig;
import xbot.common.injection.electrical_contract.CameraInfo;
import xbot.common.injection.electrical_contract.DeviceInfo;
import xbot.common.injection.electrical_contract.MotorControllerType;
import xbot.common.injection.electrical_contract.TalonFxMotorControllerOutputConfig;
import xbot.common.injection.swerve.SwerveInstance;
import xbot.common.subsystems.vision.CameraCapabilities;

import javax.inject.Inject;

import java.util.EnumSet;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Inches;

public class Contract2025 extends Contract2026 {

    protected final double simulationScalingValue = 256.0 * PoseSubsystem.INCHES_IN_A_METER;

    @Inject
    public Contract2025() {
    }

    @Override
    public boolean isDriveReady() {
        return true;
    }

    @Override
    public boolean areCanCodersReady() {
        return true;
    }

    protected String getDriveControllerName(SwerveInstance swerveInstance) {
        return "DriveSubsystem/" + swerveInstance.label() + "/Drive";
    }

    protected String getSteeringControllerName(SwerveInstance swerveInstance) {
        return "DriveSubsystem/" + swerveInstance.label() + "/Steering";
    }

    protected String getSteeringEncoderControllerName(SwerveInstance swerveInstance) {
        return "DriveSubsystem/" + swerveInstance.label() + "/SteeringEncoder";
    }

    TalonFxMotorControllerOutputConfig regularDriveMotorConfig =
            new TalonFxMotorControllerOutputConfig()
                    .withInversionType(CANMotorControllerOutputConfig.InversionType.Normal)
                    .withStatorCurrentLimit(Amps.of(80))
                    .withNeutralMode(CANMotorControllerOutputConfig.NeutralMode.Brake);

    TalonFxMotorControllerOutputConfig invertedDriveMotorConfig =
            new TalonFxMotorControllerOutputConfig()
                    .withInversionType(CANMotorControllerOutputConfig.InversionType.Inverted)
                    .withStatorCurrentLimit(Amps.of(80))
                    .withNeutralMode(CANMotorControllerOutputConfig.NeutralMode.Brake);

    @Override
    public CANMotorControllerInfo getDriveMotor(SwerveInstance swerveInstance) {
        return switch (swerveInstance.label()) {
            case "FrontRightDrive" -> new CANMotorControllerInfo(
                    getDriveControllerName(swerveInstance),
                    MotorControllerType.TalonFx,
                    CANBusId.Canivore,
                    39,
                    regularDriveMotorConfig.withStatorCurrentLimit(Amps.of(60)));
            case "RearRightDrive" -> new CANMotorControllerInfo(
                    getDriveControllerName(swerveInstance),
                    MotorControllerType.TalonFx,
                    CANBusId.Canivore,
                    31,
                    regularDriveMotorConfig.withStatorCurrentLimit(Amps.of(60)));
            case "RearLeftDrive" -> new CANMotorControllerInfo(
                    getDriveControllerName(swerveInstance),
                    MotorControllerType.TalonFx,
                    CANBusId.Canivore,
                    20,
                    regularDriveMotorConfig.withStatorCurrentLimit(Amps.of(60)));
            case "FrontLeftDrive" -> new CANMotorControllerInfo(
                    getDriveControllerName(swerveInstance),
                    MotorControllerType.TalonFx,
                    CANBusId.Canivore,
                    29,
                    regularDriveMotorConfig.withStatorCurrentLimit(Amps.of(60)));
            default -> null;
        };
    }

    @Override
    public CANMotorControllerInfo getSteeringMotor(SwerveInstance swerveInstance) {

        TalonFxMotorControllerOutputConfig invertedSteeringMotorConfig =
                new TalonFxMotorControllerOutputConfig()
                        .withInversionType(CANMotorControllerOutputConfig.InversionType.Inverted)
                        .withStatorCurrentLimit(Amps.of(45))
                        .withNeutralMode(CANMotorControllerOutputConfig.NeutralMode.Brake);

        return switch (swerveInstance.label()) {
            case "FrontRightDrive" -> new CANMotorControllerInfo(
                    getSteeringControllerName(swerveInstance),
                    MotorControllerType.TalonFx,
                    CANBusId.Canivore,
                    38,
                    invertedSteeringMotorConfig.withStatorCurrentLimit(Amps.of(40)));
            case "RearRightDrive" -> new CANMotorControllerInfo(
                    getSteeringControllerName(swerveInstance),
                    MotorControllerType.TalonFx,
                    CANBusId.Canivore,
                    30,
                    invertedSteeringMotorConfig.withStatorCurrentLimit(Amps.of(40)));
            case "RearLeftDrive" -> new CANMotorControllerInfo(
                    getSteeringControllerName(swerveInstance),
                    MotorControllerType.TalonFx,
                    CANBusId.Canivore,
                    21,
                    invertedSteeringMotorConfig.withStatorCurrentLimit(Amps.of(40)));
            case "FrontLeftDrive" -> new CANMotorControllerInfo(
                    getSteeringControllerName(swerveInstance),
                    MotorControllerType.TalonFx,
                    CANBusId.Canivore,
                    28,
                    invertedSteeringMotorConfig.withStatorCurrentLimit(Amps.of(40)));
            default -> null;
        };
    }

    @Override
    public DeviceInfo getSteeringEncoder(SwerveInstance swerveInstance) {
        double simulationScalingValue = 1.0;

        return switch (swerveInstance.label()) {
            case "FrontRightDrive" ->
                    new DeviceInfo(getSteeringEncoderControllerName(swerveInstance), CANBusId.Canivore, 54, false);
            case "RearRightDrive" ->
                    new DeviceInfo(getSteeringEncoderControllerName(swerveInstance), CANBusId.Canivore, 53, false);
            case "RearLeftDrive" ->
                    new DeviceInfo(getSteeringEncoderControllerName(swerveInstance), CANBusId.Canivore, 52, false);
            case "FrontLeftDrive" ->
                    new DeviceInfo(getSteeringEncoderControllerName(swerveInstance), CANBusId.Canivore, 51, false);
            default -> null;
        };
    }

    @Override
    public Translation2d getSwerveModuleOffsets(SwerveInstance swerveInstance) {
        // Update these XYPairs with the swerve module locations!!! (In inches)
        return switch (swerveInstance.label()) {
            case "FrontLeftDrive" -> new Translation2d(Inches.of(12), Inches.of(12));
            case "FrontRightDrive" -> new Translation2d(Inches.of(12), Inches.of(-12));
            case "RearLeftDrive" -> new Translation2d(Inches.of(-12), Inches.of(12));
            case "RearRightDrive" -> new Translation2d(Inches.of(-12), Inches.of(-12));
            default -> new Translation2d(0, 0);
        };
    }

    @Override
    public double getSteeringGearRatio() {
        return 12.1; // Documented value for WCP x2i.
    }

    @Override
    public double getDriveGearRatio() {
        return 6.48; // Documented value for WCP x2i with X3 10t gears.
    }

    private static double frontAprilCameraXDisplacement = 10.14 / PoseSubsystem.INCHES_IN_A_METER;
    private static double frontAprilCameraYDisplacement = 6.535 / PoseSubsystem.INCHES_IN_A_METER;
    private static double frontAprilCameraZDisplacement = 6.7 / PoseSubsystem.INCHES_IN_A_METER;
    private static double frontAprilCameraPitch = Math.toRadians(-21);
    private static double frontAprilCameraYaw = Math.toRadians(0);

    @Override
    public CameraInfo[] getCameraInfo() {
        return new CameraInfo[] {// {};

                new CameraInfo("Apriltag_FrontLeft_Camera",
                        "AprilTagFrontLeft",
                        new Transform3d(new Translation3d(
                                frontAprilCameraXDisplacement,
                                frontAprilCameraYDisplacement,
                                frontAprilCameraZDisplacement),
                                new Rotation3d(0, frontAprilCameraPitch, frontAprilCameraYaw)),
                        EnumSet.of(CameraCapabilities.APRIL_TAG)),

                new CameraInfo("Apriltag_FrontRight_Camera",
                        "AprilTagFrontRight",
                        new Transform3d(new Translation3d(
                                frontAprilCameraXDisplacement,
                                -frontAprilCameraYDisplacement,
                                frontAprilCameraZDisplacement),
                                new Rotation3d(0, frontAprilCameraPitch, frontAprilCameraYaw)),
                        EnumSet.of(CameraCapabilities.APRIL_TAG)),
                new CameraInfo("Apriltag_Back_Camera",
                        "AprilTagBack",
                        new Transform3d(new Translation3d(
                                -0.55 / PoseSubsystem.INCHES_IN_A_METER,
                                -0.25 / PoseSubsystem.INCHES_IN_A_METER,
                                6.3 / PoseSubsystem.INCHES_IN_A_METER),
                                new Rotation3d(0, Math.toRadians(-14.5), Math.PI)),
                        EnumSet.of(CameraCapabilities.APRIL_TAG),
                        false),

                new CameraInfo("color_camera_ov9782",
                        "AprilTagBack",
                        new Transform3d(new Translation3d(
                                -0.55 / PoseSubsystem.INCHES_IN_A_METER,
                                -0.25 / PoseSubsystem.INCHES_IN_A_METER,
                                6.3 / PoseSubsystem.INCHES_IN_A_METER),
                                new Rotation3d(0, Math.toRadians(-14.5), Math.PI)),
                        EnumSet.of(CameraCapabilities.GAME_SPECIFIC)),
        };
    }
}
