package competition.electrical_contract;

import javax.inject.Inject;

import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;
import xbot.common.injection.electrical_contract.CANBusId;
import xbot.common.injection.electrical_contract.CANLightControllerInfo;
import xbot.common.injection.electrical_contract.CANLightControllerOutputConfig;
import xbot.common.injection.electrical_contract.CANMotorControllerInfo;
import xbot.common.injection.electrical_contract.CANMotorControllerOutputConfig;
import xbot.common.injection.electrical_contract.CameraInfo;
import xbot.common.injection.electrical_contract.DeviceInfo;
import xbot.common.injection.electrical_contract.LEDStripType;
import xbot.common.injection.electrical_contract.LightControllerType;
import xbot.common.injection.electrical_contract.MotorControllerType;
import xbot.common.injection.electrical_contract.PDHPort;
import xbot.common.injection.electrical_contract.TalonFxMotorControllerOutputConfig;
import xbot.common.injection.swerve.SwerveInstance;
import xbot.common.subsystems.vision.CameraCapabilities;

import java.util.EnumSet;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Inches;

public class Contract2026 extends ElectricalContract {

    protected final double simulationScalingValue = 256.0 * PoseSubsystem.INCHES_IN_A_METER;

    @Inject
    public Contract2026() {}

    @Override
    public boolean isDriveReady() { return true; }

    @Override
    public boolean areCanCodersReady() { return true; }

    @Override
    public boolean isClimberLeftReady() { return true; }

    @Override
    public CANMotorControllerInfo getClimberMotorLeft() {
        return new CANMotorControllerInfo("ClimberMotorLeft",
                MotorControllerType.TalonFx,
                CANBusId.Canivore,
                25,
                PDHPort.PDH05,
                new TalonFxMotorControllerOutputConfig()
                        .withStatorCurrentLimit(Amps.of(10)));
    }

    @Override
    public boolean isClimberRightReady() { return true; }

    @Override
    public CANMotorControllerInfo getClimberMotorRight() {
        return new CANMotorControllerInfo("ClimberMotorRight",
                MotorControllerType.TalonFx,
                CANBusId.Canivore,
                26,
                PDHPort.PDH06,
                new TalonFxMotorControllerOutputConfig()
                        .withInversionType(CANMotorControllerOutputConfig.InversionType.Inverted)
                        .withStatorCurrentLimit(Amps.of(10)));
    }

    @Override
    public boolean isClimberAbsoluteEncoderReady() { return false; }

    @Override
    public DeviceInfo getClimberAbsoluteEncoder() {
        return new DeviceInfo("ClimberAbsoluteEncoderReady",59);

    }

    @Override                                    
    public boolean isShooterFeederReady() { return true; }

    public CANMotorControllerInfo getShooterFeederMotor() {
        return new CANMotorControllerInfo("ShooterFeederMotor",
                MotorControllerType.TalonFx,
                CANBusId.Canivore,
                37,
                PDHPort.PDH17,
                new CANMotorControllerOutputConfig()
                        .withInversionType(CANMotorControllerOutputConfig.InversionType.Inverted));
    }

    @Override
    public boolean isLeftShooterReady() { return true; }

    @Override
    public boolean isMiddleShooterReady() { return true; }

    @Override
    public boolean isRightShooterReady() { return true; }

    @Override
    public CANMotorControllerInfo getLeftShooterMotor() {
        return new CANMotorControllerInfo("ShooterLeftMotor",
                MotorControllerType.TalonFx,
                CANBusId.Canivore,
                22,
                PDHPort.PDH02,
                new CANMotorControllerOutputConfig());
    }

    @Override
    public CANMotorControllerInfo getMiddleShooterMotor() {
        return new CANMotorControllerInfo("ShooterMiddleMotor",
                MotorControllerType.TalonFx,
                CANBusId.Canivore,
                23,
                PDHPort.PDH03,
                new CANMotorControllerOutputConfig()
                        .withInversionType(CANMotorControllerOutputConfig.InversionType.Inverted));
    }

    @Override
    public CANMotorControllerInfo getRightShooterMotor() {
        return new CANMotorControllerInfo("ShooterRightMotor",
                MotorControllerType.TalonFx,
                CANBusId.Canivore,
                24,
                PDHPort.PDH04,
                new CANMotorControllerOutputConfig()
                        .withInversionType(CANMotorControllerOutputConfig.InversionType.Inverted));
    }

    @Override
    public boolean isIntakeDeployReady() { return false; }

    @Override
    public CANMotorControllerInfo getIntakeDeployMotor() {
        return new CANMotorControllerInfo("IntakeDeployMotor",
                MotorControllerType.TalonFx,
                CANBusId.Canivore,
                34,
                PDHPort.PDH14,
                new CANMotorControllerOutputConfig());
    }

    @Override
    public boolean isIntakeDeployAbsoluteEncoderReady() { return false; }

    @Override
    public DeviceInfo getIntakeDeployAbsoluteEncoderMotor() {
        return new DeviceInfo("IntakeDeployAbsoluteEncoderReady",58);
    }

    @Override
    public boolean isHoodServoLeftReady() { return true; }

    @Override
    public DeviceInfo getHoodServoLeft() {
        return new DeviceInfo("HoodServoLeft", 0);
    }

    @Override
    public boolean isHoodServoRightReady() { return true;}

    @Override
    public DeviceInfo getHoodServoRight() {
        return new DeviceInfo("HoodServoRight", 1);
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
                            MotorControllerType.TalonFx,
                            CANBusId.Canivore,
                            30,
                            PDHPort.PDH10,
                            new TalonFxMotorControllerOutputConfig().withStatorCurrentLimit(Amps.of(60)));
            case "FrontRightDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.TalonFx,
                            CANBusId.Canivore,
                            38,
                            PDHPort.PDH18,
                            new TalonFxMotorControllerOutputConfig().withStatorCurrentLimit(Amps.of(60)));
            case "RearLeftDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.TalonFx,
                            CANBusId.Canivore,
                            28,
                            PDHPort.PDH08,
                            new TalonFxMotorControllerOutputConfig().withStatorCurrentLimit(Amps.of(60)));
            case "RearRightDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.TalonFx,
                            CANBusId.Canivore,
                            20,
                            PDHPort.PDH00,
                            new TalonFxMotorControllerOutputConfig().withStatorCurrentLimit(Amps.of(60)));
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
                            MotorControllerType.TalonFx,
                            CANBusId.Canivore,
                            31,
                            PDHPort.PDH11,
                            new TalonFxMotorControllerOutputConfig()
                                    .withInversionType(CANMotorControllerOutputConfig.InversionType.Inverted)
                                    .withStatorCurrentLimit(Amps.of(40)));
            case "FrontRightDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.TalonFx,
                            CANBusId.Canivore,
                            39,
                            PDHPort.PDH19,
                            new TalonFxMotorControllerOutputConfig()
                                    .withInversionType(CANMotorControllerOutputConfig.InversionType.Inverted)
                                    .withStatorCurrentLimit(Amps.of(40)));
            case "RearLeftDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.TalonFx,
                            CANBusId.Canivore,
                            29,
                            PDHPort.PDH09,
                            new TalonFxMotorControllerOutputConfig()
                                    .withInversionType(CANMotorControllerOutputConfig.InversionType.Inverted)
                                    .withStatorCurrentLimit(Amps.of(40)));
            case "RearRightDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.TalonFx,
                            CANBusId.Canivore,
                            21,
                            PDHPort.PDH01,
                            new TalonFxMotorControllerOutputConfig()
                                    .withInversionType(CANMotorControllerOutputConfig.InversionType.Inverted)
                                    .withStatorCurrentLimit(Amps.of(40)));
            default -> null;
        };
    }

    @Override
    public boolean isFuelIntakeMotorReady() { return true; }

    @Override
    public CANMotorControllerInfo getFuelIntakeMotor() {
        return new CANMotorControllerInfo("FuelIntakeMotor",
                MotorControllerType.TalonFx,
                CANBusId.Canivore,
                32,
                PDHPort.PDH12,
                new CANMotorControllerOutputConfig()
                        .withInversionType(CANMotorControllerOutputConfig.InversionType.Inverted));
    }

    @Override
    public boolean isLightsReady() { return true; }

    @Override
    public CANLightControllerInfo getLightControllerInfo() {
        return new CANLightControllerInfo("Lights",
                LightControllerType.Candle, CANBusId.Canivore,
                57, new CANLightControllerOutputConfig(LEDStripType.GRB,
                0.15, new int[] {8}));

    }

    @Override
    public boolean isHopperRollerReady() { return true; }

    @Override
    public CANMotorControllerInfo getHopperRollerMotor() {
        return new CANMotorControllerInfo("HopperRoller",
                MotorControllerType.TalonFx,
                CANBusId.Canivore,
                33,
                PDHPort.PDH13,
                new CANMotorControllerOutputConfig());
    }

    @Override
    public DeviceInfo getSteeringEncoder(SwerveInstance swerveInstance) {
        double simulationScalingValue = 1.0;

        return switch (swerveInstance.label()) {
            case "FrontLeftDrive" ->
                    new DeviceInfo(getSteeringEncoderControllerName(swerveInstance), CANBusId.Canivore, 53, false);
            case "FrontRightDrive" ->
                    new DeviceInfo(getSteeringEncoderControllerName(swerveInstance), CANBusId.Canivore, 54, false);
            case "RearLeftDrive" ->
                    new DeviceInfo(getSteeringEncoderControllerName(swerveInstance), CANBusId.Canivore, 52, false);
            case "RearRightDrive" ->
                    new DeviceInfo(getSteeringEncoderControllerName(swerveInstance), CANBusId.Canivore, 51, false);
            default -> null;
        };
    }

    @Override
    public Translation2d getSwerveModuleOffsets(SwerveInstance swerveInstance) {
        return switch (swerveInstance.label()) {
            case "FrontLeftDrive" -> new Translation2d(Inches.of(11), Inches.of(10));
            case "FrontRightDrive" -> new Translation2d(Inches.of(11), Inches.of(-10));
            case "RearLeftDrive" -> new Translation2d(Inches.of(-11), Inches.of(10));
            case "RearRightDrive" -> new Translation2d(Inches.of(-11), Inches.of(-10));
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
        double sideAprilCameraXDisplacement = 11.1004 / PoseSubsystem.INCHES_IN_A_METER;
        double sideAprilCameraYDisplacement = 11.0 / PoseSubsystem.INCHES_IN_A_METER;
        double sideAprilCameraZDisplacement = 6.96 / PoseSubsystem.INCHES_IN_A_METER;
        double sideAprilCameraPitch = Math.toRadians(-25);

        return new CameraInfo[]{
                new CameraInfo("Apriltag_Left_Camera",
                        "AprilTagLeft",
                        new Transform3d(new Translation3d(
                                sideAprilCameraXDisplacement,
                                sideAprilCameraYDisplacement,
                                sideAprilCameraZDisplacement),
                                new Rotation3d(0, sideAprilCameraPitch, Math.toRadians(270))),
                        EnumSet.of(CameraCapabilities.APRIL_TAG)),
                new CameraInfo("Apriltag_Right_Camera",
                        "AprilTagRight",
                        new Transform3d(new Translation3d(
                                -sideAprilCameraXDisplacement,
                                sideAprilCameraYDisplacement,
                                sideAprilCameraZDisplacement),
                                new Rotation3d(0, sideAprilCameraPitch, Math.toRadians(90))),
                        EnumSet.of(CameraCapabilities.APRIL_TAG)),
                new CameraInfo("Apriltag_Front_Camera",
                        "AprilTagFront",
                        new Transform3d(new Translation3d(
                                0,
                                -1.432327 / PoseSubsystem.INCHES_IN_A_METER,
                                20.075958 / PoseSubsystem.INCHES_IN_A_METER),
                                new Rotation3d(0, Math.toRadians(-23), Math.toRadians(90))),
                        EnumSet.of(CameraCapabilities.APRIL_TAG)),
                new CameraInfo("Apriltag_Back_Camera",
                        "AprilTagBack",
                        new Transform3d(new Translation3d(
                                0,
                                12.959212 / PoseSubsystem.INCHES_IN_A_METER,
                                17.768664 / PoseSubsystem.INCHES_IN_A_METER),
                                new Rotation3d(0, Math.toRadians(-15), Math.toRadians(90))),
                        EnumSet.of(CameraCapabilities.APRIL_TAG)),
        };
    }
}
