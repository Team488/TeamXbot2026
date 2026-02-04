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
    public boolean isClimberReady() { return false; }

    public CANMotorControllerInfo getClimberMotor() {
        return new CANMotorControllerInfo("ClimberMotor",
                MotorControllerType.TalonFx,
                CANBusId.RIO,
                488,
                PDHPort.PDH00,
                new CANMotorControllerOutputConfig());
    }
                                          
    @Override                                    
    public boolean isShooterFeederReady() { return false; }

    public CANMotorControllerInfo getShooterFeederMotor() {
        return new CANMotorControllerInfo("ShooterFeederMotor",
                MotorControllerType.TalonFx,
                CANBusId.RIO,
                489,
                PDHPort.PDH00,
                new CANMotorControllerOutputConfig());
    }

    public boolean isLeftShooterReady() { return false; }

    public boolean isMiddleShooterReady() { return false; }

    public boolean isRightShooterReady() { return false; }

    public CANMotorControllerInfo getLeftShooterMotor() {
        return new CANMotorControllerInfo("ShooterMotor",
                MotorControllerType.TalonFx,
                CANBusId.RIO,
                918,
                PDHPort.PDH00,
                new CANMotorControllerOutputConfig());
    }

    public CANMotorControllerInfo getMiddleShooterMotor() {
        return new CANMotorControllerInfo("ShooterMotor",
                MotorControllerType.TalonFx,
                CANBusId.RIO,
                211,
                new CANMotorControllerOutputConfig());
    }

    public CANMotorControllerInfo getRightShooterMotor() {
        return new CANMotorControllerInfo("ShooterMotor",
                MotorControllerType.TalonFx,
                CANBusId.RIO,
                900,
                new CANMotorControllerOutputConfig());
    }

    public boolean isIntakeDeployReady() { return false; }

    public CANMotorControllerInfo getIntakeDeployMotor() {
        return new CANMotorControllerInfo("IntakeDeployMotor",
                MotorControllerType.TalonFx,
                CANBusId.RIO,
                676767, // TODO: Change ID
                PDHPort.PDH00, // TODO: Change port
                new CANMotorControllerOutputConfig());
    }

    public boolean isIntakeDeployAbsoluteEncoderReady() { return false; }

    @Override
    public DeviceInfo getIntakeDeployAbsoluteEncoderMotor() {
        return new DeviceInfo("IntakeDeployAbsoluteEncoderReady",100);

    }

    @Override
    public boolean isHoodReady() {return false;}

    public CANMotorControllerInfo getHoodMotor() {
        return new CANMotorControllerInfo("hoodMotor",
                MotorControllerType.TalonFx,
                CANBusId.RIO,
                1000,
                PDHPort.PDH00, // TODO: Change port
                new CANMotorControllerOutputConfig());
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
                            PDHPort.PDH00, // TODO: Change port
                            new TalonFxMotorControllerOutputConfig().withStatorCurrentLimit(Amps.of(60)));
            case "FrontRightDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.TalonFx,
                            CANBusId.Canivore,
                            38,
                            PDHPort.PDH00, // TODO: Change port
                            new TalonFxMotorControllerOutputConfig().withStatorCurrentLimit(Amps.of(60)));
            case "RearLeftDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.TalonFx,
                            CANBusId.Canivore,
                            28,
                            PDHPort.PDH00, // TODO: Change port
                            new TalonFxMotorControllerOutputConfig().withStatorCurrentLimit(Amps.of(60)));
            case "RearRightDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.TalonFx,
                            CANBusId.Canivore,
                            20,
                            PDHPort.PDH00, // TODO: Change port
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
                            PDHPort.PDH00, // TODO: Change port
                            new TalonFxMotorControllerOutputConfig()
                                    .withInversionType(CANMotorControllerOutputConfig.InversionType.Inverted)
                                    .withStatorCurrentLimit(Amps.of(40)));
            case "FrontRightDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.TalonFx,
                            CANBusId.Canivore,
                            39,
                            PDHPort.PDH00, // TODO: Change port
                            new TalonFxMotorControllerOutputConfig()
                                    .withInversionType(CANMotorControllerOutputConfig.InversionType.Inverted)
                                    .withStatorCurrentLimit(Amps.of(40)));
            case "RearLeftDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.TalonFx,
                            CANBusId.Canivore,
                            29,
                            PDHPort.PDH00, // TODO: Change port
                            new TalonFxMotorControllerOutputConfig()
                                    .withInversionType(CANMotorControllerOutputConfig.InversionType.Inverted)
                                    .withStatorCurrentLimit(Amps.of(40)));
            case "RearRightDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.TalonFx,
                            CANBusId.Canivore,
                            21,
                            PDHPort.PDH00, // TODO: Change port
                            new TalonFxMotorControllerOutputConfig()
                                    .withInversionType(CANMotorControllerOutputConfig.InversionType.Inverted)
                                    .withStatorCurrentLimit(Amps.of(40)));
            default -> null;
        };
    }

    @Override
    public boolean isFuelIntakeMotorReady() { return false; }

    public CANMotorControllerInfo getFuelIntakeMotor() {
        return new CANMotorControllerInfo("FuelIntakeMotor",
                MotorControllerType.TalonFx,
                CANBusId.RIO,
                23,
                PDHPort.PDH00, // TODO: Change port
                new CANMotorControllerOutputConfig());
    }

    @Override
    public boolean isLightsReady() { return false; }

    public CANLightControllerInfo getLightControlerInfo() {
        return new CANLightControllerInfo("Lights",
                LightControllerType.Candle, CANBusId.Canivore,
                11, new CANLightControllerOutputConfig(LEDStripType.GRB,
                0.15, new int[] {8}));

    }

    @Override
    public boolean isHopperRollerReady() { return false; }

    public CANMotorControllerInfo getHopperRollerMotor() {
        return new CANMotorControllerInfo("HopperRoller",
                MotorControllerType.TalonFx,
                CANBusId.RIO,
                25,
                PDHPort.PDH00,
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
