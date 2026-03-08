package competition.electrical_contract;

import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;
import xbot.common.controls.sensors.XGyro;
import xbot.common.injection.electrical_contract.CANBusId;
import xbot.common.injection.electrical_contract.CANMotorControllerInfo;
import xbot.common.injection.electrical_contract.CANMotorControllerOutputConfig;
import xbot.common.injection.electrical_contract.CameraInfo;
import xbot.common.injection.electrical_contract.DeviceInfo;
import xbot.common.injection.electrical_contract.IMUInfo;
import xbot.common.injection.electrical_contract.MotorControllerType;
import xbot.common.injection.electrical_contract.PowerSource;
import xbot.common.injection.electrical_contract.SparkMaxMotorControllerOutputConfig;
import xbot.common.injection.swerve.SwerveInstance;
import xbot.common.subsystems.vision.CameraCapabilities;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Inches;

import java.util.EnumSet;

public class Contract2023 extends Contract2026 {

    protected final double simulationScalingValue = 256.0 * PoseSubsystem.INCHES_IN_A_METER;

    @Inject
    public Contract2023() {}

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

    @Override
    public CANMotorControllerInfo getDriveMotor(SwerveInstance swerveInstance) {
        return switch (swerveInstance.label()) {
            case "FrontLeftDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.SparkMax,
                            CANBusId.RIO,
                            31,
                            new SparkMaxMotorControllerOutputConfig()
                                    .withSmartCurrentLimit(Amps.of(60)));
            case "FrontRightDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.SparkMax,
                            CANBusId.RIO,
                            29,
                            new SparkMaxMotorControllerOutputConfig()
                                    .withSmartCurrentLimit(Amps.of(60)));
            case "RearLeftDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.SparkMax,
                            CANBusId.RIO,
                            38,
                            new SparkMaxMotorControllerOutputConfig()
                                    .withSmartCurrentLimit(Amps.of(60)));
            case "RearRightDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.SparkMax,
                            CANBusId.RIO,
                            21,
                            new SparkMaxMotorControllerOutputConfig()
                                    .withSmartCurrentLimit(Amps.of(60)));
            default -> null;
        };
    }

    @Override
    public CANMotorControllerInfo getSteeringMotor(SwerveInstance swerveInstance) {
        double simulationScalingValue = 1.0;

        return switch (swerveInstance.label()) {
            case "FrontLeftDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.SparkMax,
                            CANBusId.RIO,
                            30,
                            new SparkMaxMotorControllerOutputConfig()
                                    .withSmartCurrentLimit(Amps.of(40)));
            case "FrontRightDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.SparkMax,
                            CANBusId.RIO,
                            28,
                            new SparkMaxMotorControllerOutputConfig()
                                    .withSmartCurrentLimit(Amps.of(40)));
            case "RearLeftDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.SparkMax,
                            CANBusId.RIO,
                            39,
                            new SparkMaxMotorControllerOutputConfig()
                                    .withSmartCurrentLimit(Amps.of(40)));
            case "RearRightDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.SparkMax,
                            CANBusId.RIO,
                            20,
                            new SparkMaxMotorControllerOutputConfig()
                                    .withSmartCurrentLimit(Amps.of(40)));
            default -> null;
        };
    }

    @Override
    public DeviceInfo getSteeringEncoder(SwerveInstance swerveInstance) {
        double simulationScalingValue = 1.0;

        return switch (swerveInstance.label()) {
            case "FrontLeftDrive" ->
                    new DeviceInfo(getSteeringEncoderControllerName(swerveInstance), 51, false, simulationScalingValue);
            case "FrontRightDrive" ->
                    new DeviceInfo(getSteeringEncoderControllerName(swerveInstance), 52, false, simulationScalingValue);
            case "RearLeftDrive" ->
                    new DeviceInfo(getSteeringEncoderControllerName(swerveInstance), 53, false, simulationScalingValue);
            case "RearRightDrive" ->
                    new DeviceInfo(getSteeringEncoderControllerName(swerveInstance), 54, false, simulationScalingValue);
            default -> null;
        };
    }

    @Override
    public Translation2d getSwerveModuleOffsets(SwerveInstance swerveInstance) {
        // Update these XYPairs with the swerve module locations!!! (In inches)
        return switch (swerveInstance.label()) {
            case "FrontLeftDrive" -> new Translation2d(Inches.of(15), Inches.of(15));
            case "FrontRightDrive" -> new Translation2d(Inches.of(15), Inches.of(-15));
            case "RearLeftDrive" -> new Translation2d(Inches.of(-15), Inches.of(15));
            case "RearRightDrive" -> new Translation2d(Inches.of(-15), Inches.of(-15));
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

    @Override
    public CameraInfo[] getCameraInfo() {
        double sideAprilCameraXDisplacement = 6.75 / PoseSubsystem.INCHES_IN_A_METER;
        double sideAprilCameraYDisplacement = 7.75 / PoseSubsystem.INCHES_IN_A_METER;
        double sideAprilCameraZDisplacement = 11 / PoseSubsystem.INCHES_IN_A_METER;
        double sideAprilCameraPitch = Math.toRadians(-25);

        return new CameraInfo[]{
                new CameraInfo("Apriltag_Left_Camera",
                        "AprilTagLeft",
                        new Transform3d(new Translation3d(
                                sideAprilCameraXDisplacement,
                                sideAprilCameraYDisplacement,
                                sideAprilCameraZDisplacement),
                                new Rotation3d(0, sideAprilCameraPitch, Math.toRadians(90))),
                        EnumSet.of(CameraCapabilities.APRIL_TAG)),
                new CameraInfo("Apriltag_Right_Camera",
                        "AprilTagRight",
                        new Transform3d(new Translation3d(
                                -sideAprilCameraXDisplacement,
                                sideAprilCameraYDisplacement,
                                sideAprilCameraZDisplacement),
                                new Rotation3d(0, sideAprilCameraPitch, Math.toRadians(270))),
                        EnumSet.of(CameraCapabilities.APRIL_TAG)),
                new CameraInfo("Apriltag_Front_Camera",
                        "AprilTagFront",
                        new Transform3d(new Translation3d(
                                0.0,
                                13.125 / PoseSubsystem.INCHES_IN_A_METER,
                                14.0 / PoseSubsystem.INCHES_IN_A_METER),
                                new Rotation3d(0, Math.toRadians(-30), Math.toRadians(0))),
                        EnumSet.of(CameraCapabilities.APRIL_TAG)),
                new CameraInfo("Apriltag_Back_Camera",
                        "AprilTagBack",
                        new Transform3d(new Translation3d(
                                0,
                                -12.959212 / PoseSubsystem.INCHES_IN_A_METER,
                                17.768664 / PoseSubsystem.INCHES_IN_A_METER),
                                new Rotation3d(0, Math.toRadians(-15), Math.toRadians(180))),
                        EnumSet.of(CameraCapabilities.APRIL_TAG)),
        };
    }

        @Override
    public IMUInfo getIMUInfo() {
        return new IMUInfo(XGyro.InterfaceType.spi, PowerSource.RIO);
    }

    @Override
    public boolean isLightsReady() {
        return false;
    }

    @Override
    public boolean isHopperRollerReady() {
        return false;
    }

    @Override
    public boolean isShooterFeederReady() {
        return false;
    }

    @Override
    public boolean isIntakeDeployReady() {
        return false;
    }

    @Override
    public boolean isLeftShooterReady() {
        return false;
    }

    @Override
    public boolean isMiddleShooterReady() {
        return false;
    }

    @Override
    public boolean isRightShooterReady() {
        return false;
    }

    @Override
    public boolean isHoodServoLeftReady() {
        return false;
    }

    @Override
    public boolean isHoodServoRightReady() {
        return false;
    }

    @Override
    public boolean isClimberLeftReady() {
        return false;
    }

    @Override
    public boolean isClimberRightReady() {
        return false;
    }
}
