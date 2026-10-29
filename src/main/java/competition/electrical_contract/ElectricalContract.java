package competition.electrical_contract;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.units.measure.Distance;
import xbot.common.injection.electrical_contract.CANLightControllerInfo;
import xbot.common.injection.electrical_contract.CANMotorControllerInfo;
import xbot.common.injection.electrical_contract.DeviceInfo;
import xbot.common.injection.electrical_contract.IMUInfo;
import xbot.common.injection.electrical_contract.PDHPort;
import xbot.common.injection.electrical_contract.XCameraElectricalContract;
import xbot.common.injection.electrical_contract.XSwerveDriveElectricalContract;
import xbot.common.injection.swerve.SwerveInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class ElectricalContract implements XSwerveDriveElectricalContract, XCameraElectricalContract {
    private final Set<Hardware> readinessSet;

    protected ElectricalContract(Set<Hardware> readinessSet) {
        this.readinessSet = readinessSet;
    }

    public boolean isReady(Hardware hardware) {
        return readinessSet.contains(hardware);
    }

    // TODO: Remove these later
    public abstract boolean isDriveReady();
    public abstract boolean areCanCodersReady();

    public abstract CANMotorControllerInfo getDriveMotor(SwerveInstance swerveInstance);
    public abstract CANMotorControllerInfo getSteeringMotor(SwerveInstance swerveInstance);
    public abstract DeviceInfo getSteeringEncoder(SwerveInstance swerveInstance);
    public abstract Translation2d getSwerveModuleOffsets(SwerveInstance swerveInstance);
    public abstract IMUInfo getIMUInfo();

    public abstract CANMotorControllerInfo getLeftShooterMotor();
    public abstract CANMotorControllerInfo getMiddleShooterMotor();
    public abstract CANMotorControllerInfo getRightShooterMotor();

    public abstract DeviceInfo getHoodServoLeft();
    public abstract DeviceInfo getHoodServoRight();

    public abstract CANMotorControllerInfo getIntakeDeployMotor();
    public abstract DeviceInfo getIntakeDeployAbsoluteEncoder();

    public abstract  CANMotorControllerInfo getClimberMotorLeft();
    public abstract CANMotorControllerInfo getClimberMotorRight();
    public abstract DeviceInfo getClimberSensor();

    public abstract CANMotorControllerInfo getShooterFeederMotor();

    public abstract CANMotorControllerInfo getFuelIntakeMotor();

    public abstract CANLightControllerInfo getLightControllerInfo();

    public abstract CANMotorControllerInfo getHopperRollerMotor();

    /**
     * Returns additional PDH connections for non-motor devices (e.g., VRMs, PCMs, buck converters, etc.)
     * Override this method in specific contract implementations to specify these connections.
     * Multiple non-motor devices may share a PDH port (e.g., two buck converters on one port).
     */
    public Map<PDHPort, List<String>> getAdditionalPDHConnections() {
        return new HashMap<>();
    }

    /**
     * Returns power branch connections for intermediate converters (e.g., buck converters, VRMs).
     * Maps converter name to the list of devices it powers.
     * Override this method in specific contract implementations to document the power chain.
     */
    public Map<String, List<String>> getAdditionalPowerBranches() {
        return new HashMap<>();
    }

    public abstract Distance getRadiusOfRobot();

    protected String getDriveControllerName(SwerveInstance swerveInstance) {
        return "DriveSubsystem/" + swerveInstance.label() + "/Drive";
    }

    protected String getSteeringControllerName(SwerveInstance swerveInstance) {
        return "DriveSubsystem/" + swerveInstance.label() + "/Steering";
    }

    protected String getSteeringEncoderControllerName(SwerveInstance swerveInstance) {
        return "DriveSubsystem/" + swerveInstance.label() + "/SteeringEncoder";
    }
}
