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
    public boolean isClimberLeftReady() { return false; }

    @Override
    public CANMotorControllerInfo getClimberMotorLeft() {
        return new CANMotorControllerInfo("ClimberMotorLeft",
                MotorControllerType.TalonFx,
                CANBusId.Canivore,
                25,
                PDHPort.PDH05,
                new TalonFxMotorControllerOutputConfig()
                        .withStatorCurrentLimit(Amps.of(80))
                        .withSupplyCurrentLimit(Amps.of(40), Amps.of(60), Seconds.of(1)));
    }

    @Override
    public boolean isClimberRightReady() { return false; }

    @Override
    public CANMotorControllerInfo getClimberMotorRight() {
        return new CANMotorControllerInfo("ClimberMotorRight",
                MotorControllerType.TalonFx,
                CANBusId.Canivore,
                26,
                PDHPort.PDH06,
                new TalonFxMotorControllerOutputConfig()
                        .withInversionType(CANMotorControllerOutputConfig.InversionType.Inverted)
                        .withStatorCurrentLimit(Amps.of(80))
                        .withSupplyCurrentLimit(Amps.of(40), Amps.of(60), Seconds.of(1)));
    }

    @Override
    public boolean isClimberAbsoluteEncoderReady() { return false; }

    @Override
    public DeviceInfo getClimberAbsoluteEncoder() {
        return new DeviceInfo("ClimberAbsoluteEncoderReady", CANBusId.Canivore, 59);
    }

    @Override
    public boolean isClimberSensorReady() { return false; }

    @Override
    public DeviceInfo getClimberSensor() {
        return new DeviceInfo("ClimberSensor", 0, PowerSource.RIO);
    }

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

    public boolean intakeDeploySensorReady() { return true; }

    public DeviceInfo getIntakeDeploySensor() {
        return new DeviceInfo("IntakeDeploySensor", 1, true, PowerSource.RIO);
    }

    @Override
    public boolean isIntakeDeployExtendedSensorReady() { return true; }

    @Override
    public DeviceInfo getIntakeDeployExtendedSensor() {return new DeviceInfo("IntakeDeployExtendedSensor", 2, true, PowerSource.RIO);}

    @Override                                    
    public boolean isShooterFeederReady() { return true; }

    public CANMotorControllerInfo getShooterFeederMotor() {
        return new CANMotorControllerInfo("ShooterFeederMotor",
                MotorControllerType.TalonFx,
                CANBusId.Canivore,
                37,
                PDHPort.PDH17,
                new TalonFxMotorControllerOutputConfig()
                        .withInversionType(CANMotorControllerOutputConfig.InversionType.Inverted)
                        .withStatorCurrentLimit(Amps.of(35)));
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
                        .withStatorCurrentLimit(Amps.of(50))
                        .withSupplyCurrentLimit(
                                Amps.of(40),
                                Amps.of(60),
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
                        .withInversionType(CANMotorControllerOutputConfig.InversionType.Normal)
                        .withStatorCurrentLimit(Amps.of(50))
                        .withSupplyCurrentLimit(
                                Amps.of(40),
                                Amps.of(60),
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
                        .withStatorCurrentLimit(Amps.of(50))
                        .withSupplyCurrentLimit(
                                Amps.of(40),
                                Amps.of(60),
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
                        .withSupplyCurrentLimit(Amps.of(15), Amps.of(30), Seconds.of(1))
                        .withStatorCurrentLimit(Amps.of(50)));
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
        return new DeviceInfo("HoodServoLeft", 7);
    }

    @Override
    public boolean isHoodServoRightReady() { return true;}

    @Override
    public DeviceInfo getHoodServoRight() {
        return new DeviceInfo("HoodServoRight", 9);
    }
    
    // OrangePis - powered via buck converters (see getAdditionalPowerBranches)
    public DeviceInfo getFrontOrangePi() {
        return new DeviceInfo("FrontOrangePi", -1, PowerSource.NONE);
    }

    public DeviceInfo getBackOrangePi() {
        return new DeviceInfo("BackOrangePi", -1, PowerSource.NONE);
    }

    // Orin Nano - powered via FrontBuckBoost_Pwr (see getAdditionalPowerBranches)
    public DeviceInfo getOrinNano() {
        return new DeviceInfo("Orin_Nano", -1, PowerSource.NONE);
    }

    // Ethernet switch - powered via BackBuckBoost_Pwr (see getAdditionalPowerBranches)
    public DeviceInfo getEthernetSwitch() {
        return new DeviceInfo("EthernetSwitch", -1, PowerSource.NONE);
    }

    // VRM1 12V/2A outputs
    public DeviceInfo getVrm1_12v_2a() {
        return new DeviceInfo("FrontBuckBoost_Pwr", -1, PowerSource.VRM1_12V_2A);
    }

    public DeviceInfo getVrm1_12v_2b() {
        return new DeviceInfo("BackBuckBoost_Pwr", -1, PowerSource.VRM1_12V_2B);
    }

    // VRM1 12V/500mA outputs
    public DeviceInfo getVrm1_12v_500ma() {
        return new DeviceInfo("FrontBuckBoost_Fan", -1, PowerSource.VRM1_12V_500MA);
    }

    public DeviceInfo getVrm1_12v_500mb() {
        return new DeviceInfo("BackBuckBoost_Fan", -1, PowerSource.VRM1_12V_500MB);
    }

    // VRM1 5V/2A outputs - unused
    public DeviceInfo getVrm1_5v_2a() {
        return new DeviceInfo("No_Connect", -1, PowerSource.VRM1_5V_2A);
    }

    public DeviceInfo getVrm1_5v_2b() {
        return new DeviceInfo("No_Connect", -1, PowerSource.VRM1_5V_2B);
    }

    // VRM1 5V/500mA outputs
    public DeviceInfo getVrm1_5v_500ma() {
        return new DeviceInfo("CANdle", -1, PowerSource.VRM1_5V_500MA);
    }

    public DeviceInfo getVrm1_5v_500mb() {
        return new DeviceInfo("No_Connect", -1, PowerSource.VRM1_5V_500MB);
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
                            new TalonFxMotorControllerOutputConfig()
                                    .withStatorCurrentLimit(Amps.of(45)));
            case "FrontRightDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.TalonFx,
                            CANBusId.Canivore,
                            38,
                            PDHPort.PDH18,
                            new TalonFxMotorControllerOutputConfig()
                                    .withStatorCurrentLimit(Amps.of(45)));
            case "RearLeftDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.TalonFx,
                            CANBusId.Canivore,
                            28,
                            PDHPort.PDH08,
                            new TalonFxMotorControllerOutputConfig()
                                    .withStatorCurrentLimit(Amps.of(45)));
            case "RearRightDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.TalonFx,
                            CANBusId.Canivore,
                            20,
                            PDHPort.PDH00,
                            new TalonFxMotorControllerOutputConfig()
                                    .withStatorCurrentLimit(Amps.of(45)));
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
                new TalonFxMotorControllerOutputConfig()
                        .withInversionType(CANMotorControllerOutputConfig.InversionType.Inverted)
                        .withStatorCurrentLimit(Amps.of(30)));
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
                new TalonFxMotorControllerOutputConfig()
                        .withStatorCurrentLimit(Amps.of(40)));
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
