package competition.electrical_contract;

import com.revrobotics.AbsoluteEncoder;
import edu.wpi.first.math.geometry.Translation2d;
import xbot.common.injection.electrical_contract.CANBusId;
import xbot.common.injection.electrical_contract.CANLightControllerInfo;
import xbot.common.injection.electrical_contract.CANMotorControllerInfo;
import xbot.common.injection.electrical_contract.DeviceInfo;
import xbot.common.injection.electrical_contract.XCameraElectricalContract;
import xbot.common.injection.electrical_contract.XSwerveDriveElectricalContract;
import xbot.common.injection.swerve.SwerveInstance;

public abstract class ElectricalContract implements XSwerveDriveElectricalContract, XCameraElectricalContract {
    public abstract boolean isDriveReady();

    public abstract boolean areCanCodersReady();

    public abstract CANMotorControllerInfo getDriveMotor(SwerveInstance swerveInstance);

    public abstract CANMotorControllerInfo getSteeringMotor(SwerveInstance swerveInstance);

    public abstract DeviceInfo getSteeringEncoder(SwerveInstance swerveInstance);

    public abstract Translation2d getSwerveModuleOffsets(SwerveInstance swerveInstance);

    public abstract boolean isHoodReady();

    public abstract CANMotorControllerInfo getHoodMotor();

    public abstract boolean isIntakeDeployReady();

    public abstract CANMotorControllerInfo getIntakeDeployMotor();

    public abstract boolean isIntakeDeployAbsoluteEncoderReady();

    public abstract DeviceInfo getIntakeDeployAbsoluteEncoderMotor();

    public abstract boolean isShooterReady();

    public abstract CANMotorControllerInfo getShooterMotor();

    public abstract boolean isClimberReady();

    public abstract CANMotorControllerInfo getClimberMotor();

    public abstract boolean isShooterFeederReady();

    public abstract CANMotorControllerInfo getShooterFeederMotor();

    public abstract boolean isFuelIntakeMotorReady();

    public abstract CANMotorControllerInfo getFuelIntakeMotor();
  
    public abstract CANLightControllerInfo getLightControlerInfo();
}
