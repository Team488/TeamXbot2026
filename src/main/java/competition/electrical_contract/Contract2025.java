package competition.electrical_contract;

import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Translation2d;
import xbot.common.injection.electrical_contract.CANBusId;
import xbot.common.injection.electrical_contract.CANMotorControllerInfo;
import xbot.common.injection.electrical_contract.CANMotorControllerOutputConfig;
import xbot.common.injection.electrical_contract.DeviceInfo;
import xbot.common.injection.electrical_contract.MotorControllerType;
import xbot.common.injection.swerve.SwerveInstance;

import javax.inject.Inject;

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

    CANMotorControllerOutputConfig regularDriveMotorConfig =
            new CANMotorControllerOutputConfig()
                    .withInversionType(CANMotorControllerOutputConfig.InversionType.Normal)
                    .withStatorCurrentLimit(Amps.of(80))
                    .withNeutralMode(CANMotorControllerOutputConfig.NeutralMode.Brake);

    CANMotorControllerOutputConfig invertedDriveMotorConfig =
            new CANMotorControllerOutputConfig()
                    .withInversionType(CANMotorControllerOutputConfig.InversionType.Inverted)
                    .withStatorCurrentLimit(Amps.of(80))
                    .withNeutralMode(CANMotorControllerOutputConfig.NeutralMode.Brake);

    @Override
    public CANMotorControllerInfo getDriveMotor(SwerveInstance swerveInstance) {
        return switch (swerveInstance.label()) {
            case "FrontRightDrive" -> new CANMotorControllerInfo(
                    getDriveControllerName(swerveInstance),
                    MotorControllerType.TalonFx,
                    CANBusId.DefaultCanivore,
                    39,
                    regularDriveMotorConfig);
            case "RearRightDrive" -> new CANMotorControllerInfo(
                    getDriveControllerName(swerveInstance),
                    MotorControllerType.TalonFx,
                    CANBusId.DefaultCanivore,
                    31,
                    regularDriveMotorConfig);
            case "RearLeftDrive" -> new CANMotorControllerInfo(
                    getDriveControllerName(swerveInstance),
                    MotorControllerType.TalonFx,
                    CANBusId.DefaultCanivore,
                    20,
                    regularDriveMotorConfig);
            case "FrontLeftDrive" -> new CANMotorControllerInfo(
                    getDriveControllerName(swerveInstance),
                    MotorControllerType.TalonFx,
                    CANBusId.DefaultCanivore,
                    29,
                    regularDriveMotorConfig);
            default -> null;
        };
    }

    @Override
    public CANMotorControllerInfo getSteeringMotor(SwerveInstance swerveInstance) {

        CANMotorControllerOutputConfig invertedSteeringMotorConfig =
                new CANMotorControllerOutputConfig()
                        .withInversionType(CANMotorControllerOutputConfig.InversionType.Inverted)
                        .withStatorCurrentLimit(Amps.of(45))
                        .withNeutralMode(CANMotorControllerOutputConfig.NeutralMode.Brake);

        return switch (swerveInstance.label()) {
            case "FrontRightDrive" -> new CANMotorControllerInfo(
                    getSteeringControllerName(swerveInstance),
                    MotorControllerType.TalonFx,
                    CANBusId.DefaultCanivore,
                    38,
                    invertedSteeringMotorConfig);
            case "RearRightDrive" -> new CANMotorControllerInfo(
                    getSteeringControllerName(swerveInstance),
                    MotorControllerType.TalonFx,
                    CANBusId.DefaultCanivore,
                    30,
                    invertedSteeringMotorConfig);
            case "RearLeftDrive" -> new CANMotorControllerInfo(
                    getSteeringControllerName(swerveInstance),
                    MotorControllerType.TalonFx,
                    CANBusId.DefaultCanivore,
                    21,
                    invertedSteeringMotorConfig);
            case "FrontLeftDrive" -> new CANMotorControllerInfo(
                    getSteeringControllerName(swerveInstance),
                    MotorControllerType.TalonFx,
                    CANBusId.DefaultCanivore,
                    28,
                    invertedSteeringMotorConfig);
            default -> null;
        };
    }

    @Override
    public DeviceInfo getSteeringEncoder(SwerveInstance swerveInstance) {
        double simulationScalingValue = 1.0;

        return switch (swerveInstance.label()) {
            case "FrontRightDrive" ->
                    new DeviceInfo(getSteeringEncoderControllerName(swerveInstance), CANBusId.DefaultCanivore, 54, false);
            case "RearRightDrive" ->
                    new DeviceInfo(getSteeringEncoderControllerName(swerveInstance), CANBusId.DefaultCanivore, 53, false);
            case "RearLeftDrive" ->
                    new DeviceInfo(getSteeringEncoderControllerName(swerveInstance), CANBusId.DefaultCanivore, 52, false);
            case "FrontLeftDrive" ->
                    new DeviceInfo(getSteeringEncoderControllerName(swerveInstance), CANBusId.DefaultCanivore, 51, false);
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
                        false)
        };
    }
}
