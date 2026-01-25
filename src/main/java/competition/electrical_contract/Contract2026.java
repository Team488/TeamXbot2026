package competition.electrical_contract;

import javax.inject.Inject;

import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.math.geometry.Translation2d;
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
import xbot.common.injection.swerve.SwerveInstance;

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
                new CANMotorControllerOutputConfig());
    }
                                          
    @Override                                    
    public boolean isShooterFeederReady() { return false; }

    public CANMotorControllerInfo getShooterFeederMotor() {
        return new CANMotorControllerInfo("ShooterFeederMotor",
                MotorControllerType.TalonFx,
                CANBusId.RIO,
                489,
                new CANMotorControllerOutputConfig());
    }

    public boolean isShooterReady() { return false; }

    public CANMotorControllerInfo getShooterMotor() {
        return new CANMotorControllerInfo("ShooterMotor",
                MotorControllerType.TalonFx,
                CANBusId.RIO,
                918,
                new CANMotorControllerOutputConfig());
    }


    public boolean isIntakeDeployReady() { return false; }

    //TODO: change id
    public CANMotorControllerInfo getIntakeDeployMotor() {
        return new CANMotorControllerInfo("IntakeDeployMotor",
                MotorControllerType.TalonFx,
                CANBusId.RIO,
                679178, // TODO:Change ID
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
                            MotorControllerType.SparkMax,
                            CANBusId.RIO,
                            31,
                            new CANMotorControllerOutputConfig());
            case "FrontRightDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.SparkMax,
                            CANBusId.RIO,
                            29,
                            new CANMotorControllerOutputConfig());
            case "RearLeftDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.SparkMax,
                            CANBusId.RIO,
                            38,
                            new CANMotorControllerOutputConfig());
            case "RearRightDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.SparkMax,
                            CANBusId.RIO,
                            21,
                            new CANMotorControllerOutputConfig());
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
                            new CANMotorControllerOutputConfig());
            case "FrontRightDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.SparkMax,
                            CANBusId.RIO,
                            28,
                            new CANMotorControllerOutputConfig());
            case "RearLeftDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.SparkMax,
                            CANBusId.RIO,
                            39,
                            new CANMotorControllerOutputConfig());
            case "RearRightDrive" ->
                    new CANMotorControllerInfo(
                            getDriveControllerName(swerveInstance),
                            MotorControllerType.SparkMax,
                            CANBusId.RIO,
                            20,
                            new CANMotorControllerOutputConfig());
            default -> null;
        };
    }

    @Override
    public boolean isFuelIntakeMotorReady() { return true; }

    public CANMotorControllerInfo getFuelIntakeMotor() {
        return new CANMotorControllerInfo("FuelIntakeMotor",
                MotorControllerType.TalonFx,
                CANBusId.RIO,
                23,
                new CANMotorControllerOutputConfig());
    }

    @Override
    public CANLightControllerInfo getLightControlerInfo() {
        return new CANLightControllerInfo("Lights",
                LightControllerType.Candle, CANBusId.Canivore,
                11, new CANLightControllerOutputConfig(LEDStripType.GRB,
                0.15, new int[] {8}));

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

    public CameraInfo[] getCameraInfo() {
        return new CameraInfo[] { /* TODO: No cameras defined yet in 2026 */ };
    }
}
