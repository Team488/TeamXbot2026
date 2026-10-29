package competition.electrical_contract;

import javax.inject.Inject;

import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.units.Units;
import xbot.common.controls.sensors.XGyro;
import xbot.common.injection.electrical_contract.CANBusId;
import xbot.common.injection.electrical_contract.CANLightControllerInfo;
import xbot.common.injection.electrical_contract.CANLightControllerOutputConfig;
import xbot.common.injection.electrical_contract.CANMotorControllerInfo;
import xbot.common.injection.electrical_contract.CANMotorControllerOutputConfig;
import xbot.common.injection.electrical_contract.CameraInfo;
import xbot.common.injection.electrical_contract.DeviceInfo;
import xbot.common.injection.electrical_contract.IMUInfo;
import xbot.common.injection.electrical_contract.LEDStripType;
import xbot.common.injection.electrical_contract.LightControllerType;
import xbot.common.injection.electrical_contract.MotorControllerType;
import xbot.common.injection.electrical_contract.PDHPort;
import xbot.common.injection.electrical_contract.PowerSource;
import xbot.common.injection.electrical_contract.TalonFxMotorControllerOutputConfig;
import xbot.common.injection.swerve.SwerveInstance;
import xbot.common.subsystems.vision.CameraCapabilities;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Inches;
import static edu.wpi.first.units.Units.Seconds;

public class Contract2026 extends GeneralContract {

    @Inject
    public Contract2026() {
        super(Set.of(
                Hardware.LeftShooter,
                Hardware.MiddleShooter,
                Hardware.RightShooter,
                Hardware.ShooterFeeder,
                Hardware.IntakeDeploy,
                Hardware.IntakeDeployEncoder,
                Hardware.FuelIntake,
                Hardware.HopperRoller,
                Hardware.HoodServoLeft,
                Hardware.HoodServoRight,
                Hardware.Lights
        ));
    }

    protected Contract2026(Set<Hardware> readinessSet) {
        super(readinessSet);
    }

    @Override
    public boolean isDriveReady() { return true; }

    @Override
    public boolean areCanCodersReady() { return true; }

    @Override
    public IMUInfo getIMUInfo() {
        return new IMUInfo(XGyro.InterfaceType.spi, PowerSource.RIO);
    }

    public DeviceInfo pigeon2() {
        return new DeviceInfo("Pigeon2.0",CANBusId.Canivore, 56);
    }

    public DeviceInfo candle() {
        return new DeviceInfo("CANdle",CANBusId.Canivore, 57);
    }

    @Override
    public CANMotorControllerInfo getDriveMotor(SwerveInstance swerveInstance) {
        var motorConfig = new TalonFxMotorControllerOutputConfig()
                .withNeutralMode(CANMotorControllerOutputConfig.NeutralMode.Brake)
                .withStatorCurrentLimit(Amps.of(45));
        return switch (swerveInstance.label()) {
            case "FrontLeftDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.TalonFx,
                            CANBusId.Canivore,
                            30,
                            PDHPort.PDH10,
                            motorConfig);
            case "FrontRightDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.TalonFx,
                            CANBusId.Canivore,
                            38,
                            PDHPort.PDH18,
                            motorConfig);
            case "RearLeftDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.TalonFx,
                            CANBusId.Canivore,
                            28,
                            PDHPort.PDH08,
                            motorConfig);
            case "RearRightDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.TalonFx,
                            CANBusId.Canivore,
                            20,
                            PDHPort.PDH00,
                            motorConfig);
            default -> null;
        };
    }

    @Override
    public CANMotorControllerInfo getSteeringMotor(SwerveInstance swerveInstance) {
        var motorConfig = new TalonFxMotorControllerOutputConfig()
                .withInversionType(CANMotorControllerOutputConfig.InversionType.Inverted)
                .withStatorCurrentLimit(Amps.of(40));

        return switch (swerveInstance.label()) {
            case "FrontLeftDrive" ->
                    new CANMotorControllerInfo(
                            getSteeringControllerName(swerveInstance),
                            MotorControllerType.TalonFx,
                            CANBusId.Canivore,
                            31,
                            PDHPort.PDH11,
                            motorConfig);
            case "FrontRightDrive" ->
                    new CANMotorControllerInfo(
                            getSteeringControllerName(swerveInstance),
                            MotorControllerType.TalonFx,
                            CANBusId.Canivore,
                            39,
                            PDHPort.PDH19,
                            motorConfig);
            case "RearLeftDrive" ->
                    new CANMotorControllerInfo(
                            getSteeringControllerName(swerveInstance),
                            MotorControllerType.TalonFx,
                            CANBusId.Canivore,
                            29,
                            PDHPort.PDH09,
                            motorConfig);
            case "RearRightDrive" ->
                    new CANMotorControllerInfo(
                            getSteeringControllerName(swerveInstance),
                            MotorControllerType.TalonFx,
                            CANBusId.Canivore,
                            21,
                            PDHPort.PDH01,
                            motorConfig);
            default -> null;
        };
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
        return 5.40; // Documented value for WCP x2i with X3 12t gears.
    }

    // --- CAN Bus Connection Order (used only by the electrical report tool) ---
    // Lists every CAN device with its physical daisy-chain position.
    // busId + canId must uniquely identify the device returned by the corresponding getter.
    public static record CanBusOrderEntry(CANBusId busId, int canId, String deviceName, int busPosition) {}

    public List<CanBusOrderEntry> getCanBusConnectionOrder() {
        return List.of(
            new CanBusOrderEntry(CANBusId.Canivore, 30, "FrontLeftDrive/Drive",              1),
            new CanBusOrderEntry(CANBusId.Canivore, 53, "FrontLeftDrive/SteeringEncoder",    2),
            new CanBusOrderEntry(CANBusId.Canivore, 31, "FrontLeftDrive/Steering",           3),
            new CanBusOrderEntry(CANBusId.Canivore, 29, "RearLeftDrive/Steering",            4),
            new CanBusOrderEntry(CANBusId.Canivore, 52, "RearLeftDrive/SteeringEncoder",     5),
            new CanBusOrderEntry(CANBusId.Canivore, 28, "RearLeftDrive/Drive",               6),
            new CanBusOrderEntry(CANBusId.Canivore, 20, "RearRightDrive/Drive",              7),
            new CanBusOrderEntry(CANBusId.Canivore, 51, "RearRightDrive/SteeringEncoder",    8),
            new CanBusOrderEntry(CANBusId.Canivore, 21, "RearRightDrive/Steering",           9),
            new CanBusOrderEntry(CANBusId.Canivore, 39, "FrontRightDrive/Steering",         10),
            new CanBusOrderEntry(CANBusId.Canivore, 54, "FrontRightDrive/SteeringEncoder",  11),
            new CanBusOrderEntry(CANBusId.Canivore, 38, "FrontRightDrive/Drive",            12),
            new CanBusOrderEntry(CANBusId.Canivore, 56, "Pigeon",                           13),
            new CanBusOrderEntry(CANBusId.Canivore, 57, "CANdle",                           14),
            new CanBusOrderEntry(CANBusId.Canivore, 58, "IntakeDeployAbsoluteEncoderReady", 15),
            new CanBusOrderEntry(CANBusId.Canivore, 34, "IntakeDeployMotor",                16),
            new CanBusOrderEntry(CANBusId.Canivore, 32, "FuelIntakeMotor",                  17),
            new CanBusOrderEntry(CANBusId.Canivore, 33, "HopperRoller",                     18),
            new CanBusOrderEntry(CANBusId.Canivore, 37, "ShooterFeederMotor",               19),
            new CanBusOrderEntry(CANBusId.Canivore, 25, "ClimberMotorLeft",                 20),
            new CanBusOrderEntry(CANBusId.Canivore, 22, "ShooterLeftMotor",                 21),
            new CanBusOrderEntry(CANBusId.Canivore, 23, "ShooterMiddleMotor",               22),
            new CanBusOrderEntry(CANBusId.Canivore, 59, "ClimberAbsoluteEncoderReady",      23),
            new CanBusOrderEntry(CANBusId.Canivore, 24, "ShooterRightMotor",                24),
            new CanBusOrderEntry(CANBusId.Canivore, 26, "ClimberMotorRight",                25)
        );
    }

    @Override
    public Map<PDHPort, List<String>> getAdditionalPDHConnections() {
        Map<PDHPort, List<String>> connections = new HashMap<>();
        connections.put(PDHPort.PDH07, List.of("No_Connect"));
        connections.put(PDHPort.PDH15, List.of("FrontBuck_Pwr"));
        connections.put(PDHPort.PDH16, List.of("BackBuck_Pwr"));
        connections.put(PDHPort.PDH20, List.of("RoboRio"));
        connections.put(PDHPort.PDH21, List.of("VRM1"));
        connections.put(PDHPort.PDH22, List.of("Radio_Power_Module"));
        connections.put(PDHPort.PDH23, List.of("Pigeon"));
        return connections;
    }

    @Override
    public Map<String, List<String>> getAdditionalPowerBranches() {
        Map<String, List<String>> branches = new HashMap<>();
        branches.put("FrontBuck_Pwr", List.of("FrontOrangePi"));
        branches.put("BackBuck_Pwr", List.of("BackOrangePi"));
        branches.put("FrontBuckBoost_Pwr", List.of("Orin_Nano"));
        branches.put("BackBuckBoost_Pwr", List.of("EthernetSwitch"));
        return branches;
    }

    @Override
    public Distance getRadiusOfRobot() {
        return Units.Inches.of(18);
    }

    @Override
    public CameraInfo[] getCameraInfo() {
        double sideAprilCameraXDisplacement = -0.28;
        double sideAprilCameraYDisplacement = 0.2965;
        double sideAprilCameraZDisplacement = 0.19;
        double sideAprilCameraPitch = Math.toRadians(-25.5);

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
                                sideAprilCameraXDisplacement,
                                -sideAprilCameraYDisplacement,
                                sideAprilCameraZDisplacement),
                                new Rotation3d(0, sideAprilCameraPitch, Math.toRadians(270))),
                        EnumSet.of(CameraCapabilities.APRIL_TAG)),
                new CameraInfo("Apriltag_Front_Camera",
                        "AprilTagFront",
                        new Transform3d(new Translation3d(
                                0,
                                0.25 / PoseSubsystem.INCHES_IN_A_METER,
                                20.075958 / PoseSubsystem.INCHES_IN_A_METER),
                                new Rotation3d(0, Math.toRadians(-23), Math.toRadians(0))),
                        EnumSet.of(CameraCapabilities.APRIL_TAG)),
                new CameraInfo("Apriltag_Back_Camera",
                        "AprilTagBack",
                        new Transform3d(new Translation3d(
                                -0.3429,
                                0,
                                0.487),
                                new Rotation3d(0, Math.toRadians(-15), Math.toRadians(180))),
                        EnumSet.of(CameraCapabilities.APRIL_TAG)),
        };
    }
}
