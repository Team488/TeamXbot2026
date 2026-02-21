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
import xbot.common.injection.electrical_contract.PowerSource;
import xbot.common.injection.electrical_contract.TalonFxMotorControllerOutputConfig;
import xbot.common.injection.swerve.SwerveInstance;
import xbot.common.subsystems.vision.CameraCapabilities;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Inches;
import static edu.wpi.first.units.Units.Seconds;

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
                new TalonFxMotorControllerOutputConfig()
                        .withStatorCurrentLimit(Amps.of(100))
                        .withSupplyCurrentLimit(
                                Amps.of(60),
                                Amps.of(80),
                                Seconds.of(1)));
    }

    @Override
    public CANMotorControllerInfo getMiddleShooterMotor() {
        return new CANMotorControllerInfo("ShooterMiddleMotor",
                MotorControllerType.TalonFx,
                CANBusId.Canivore,
                23,
                PDHPort.PDH03,
                new TalonFxMotorControllerOutputConfig()
                        .withInversionType(CANMotorControllerOutputConfig.InversionType.Inverted)
                        .withStatorCurrentLimit(Amps.of(100))
                        .withSupplyCurrentLimit(
                                Amps.of(60),
                                Amps.of(80),
                                Seconds.of(1)));
    }

    @Override
    public CANMotorControllerInfo getRightShooterMotor() {
        return new CANMotorControllerInfo("ShooterRightMotor",
                MotorControllerType.TalonFx,
                CANBusId.Canivore,
                24,
                PDHPort.PDH04,
                new TalonFxMotorControllerOutputConfig()
                        .withInversionType(CANMotorControllerOutputConfig.InversionType.Inverted)
                        .withStatorCurrentLimit(Amps.of(100))
                        .withSupplyCurrentLimit(
                                Amps.of(60),
                                Amps.of(80),
                                Seconds.of(1)));
    }

    @Override
    public boolean isIntakeDeployReady() { return true; }

    @Override
    public CANMotorControllerInfo getIntakeDeployMotor() {
        return new CANMotorControllerInfo("IntakeDeployMotor",
                MotorControllerType.TalonFx,
                CANBusId.Canivore,
                34,
                PDHPort.PDH14,
                new TalonFxMotorControllerOutputConfig()
                        .withStatorCurrentLimit(Amps.of(60)));
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

    // OrangePi single board computers - powered via buck converters (see getAdditionalPowerBranches)
    public DeviceInfo getOrangePi1() {
        return new DeviceInfo("OrangePi1", -1, PowerSource.NONE);
    }

    public DeviceInfo getOrangePi2() {
        return new DeviceInfo("OrangePi2", -1, PowerSource.NONE);
    }

    // Neo motor powered via BuckBoost1 (see getAdditionalPowerBranches)
    public DeviceInfo getNeo() {
        return new DeviceInfo("Neo", -1, PowerSource.NONE);
    }

    // VRM1 5V/2A outputs
    public DeviceInfo getRadio() {
        return new DeviceInfo("Radio", -1, PowerSource.VRM1_5V_2A);
    }

    public DeviceInfo getDev2() {
        return new DeviceInfo("Dev2", -1, PowerSource.VRM1_5V_2B);
    }

    // VRM1 12V/2A output
    public DeviceInfo getDev3() {
        return new DeviceInfo("Dev3", -1, PowerSource.VRM1_12V_2A);
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
    public Map<PDHPort, List<String>> getAdditionalPDHConnections() {
        Map<PDHPort, List<String>> connections = new HashMap<>();
        connections.put(PDHPort.PDH16, List.of("VRM1"));
        connections.put(PDHPort.PDH07, List.of("BuckBoost1"));
        connections.put(PDHPort.PDH15, List.of("BuckPwr1", "BuckPwr2")); // Multiple non-motor connections are allowed
        return connections;
    }

    @Override
    public Map<String, List<String>> getAdditionalPowerBranches() {
        Map<String, List<String>> branches = new HashMap<>();
        branches.put("BuckPwr1", List.of("OrangePi1"));
        branches.put("BuckPwr2", List.of("OrangePi2"));
        branches.put("BuckBoost1", List.of("Neo"));
        return branches;
    }

    @Override
    public CameraInfo[] getCameraInfo() {
        // TODO: These camera positions are a placeholder for simulator-based testing.
        double frontAprilCameraXDisplacement = 10.14 / PoseSubsystem.INCHES_IN_A_METER;
        double frontAprilCameraYDisplacement = 0 / PoseSubsystem.INCHES_IN_A_METER;
        double frontAprilCameraZDisplacement = 6.7 / PoseSubsystem.INCHES_IN_A_METER;
        double frontAprilCameraPitch = Math.toRadians(-21);
        double frontAprilCameraYaw = Math.toRadians(0);

        return new CameraInfo[]{
                new CameraInfo("Apriltag_Front",
                        "AprilTagFront",
                        new Transform3d(new Translation3d(
                                frontAprilCameraXDisplacement,
                                frontAprilCameraYDisplacement,
                                frontAprilCameraZDisplacement),
                                new Rotation3d(0, frontAprilCameraPitch, frontAprilCameraYaw)),
                        EnumSet.of(CameraCapabilities.APRIL_TAG))
        };
    }
}
