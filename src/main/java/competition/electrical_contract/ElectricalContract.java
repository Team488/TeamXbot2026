package competition.electrical_contract;

import edu.wpi.first.math.geometry.Translation2d;
import xbot.common.injection.electrical_contract.CANMotorControllerInfo;
import xbot.common.injection.electrical_contract.DeviceInfo;
import xbot.common.injection.electrical_contract.XSwerveDriveElectricalContract;
import xbot.common.injection.swerve.SwerveInstance;

public abstract class ElectricalContract implements XSwerveDriveElectricalContract {
    public abstract boolean isDriveReady();

    public abstract boolean areCanCodersReady();

    public abstract CANMotorControllerInfo getDriveMotor(SwerveInstance swerveInstance);

    public abstract CANMotorControllerInfo getSteeringMotor(SwerveInstance swerveInstance);

    public abstract DeviceInfo getSteeringEncoder(SwerveInstance swerveInstance);

    public abstract Translation2d getSwerveModuleOffsets(SwerveInstance swerveInstance);

    public abstract boolean isCollectDeployReady();

    public abstract CANMotorControllerInfo getCollectDeployMotor();
  
    public abstract boolean isShooterReady();

    public abstract CANMotorControllerInfo getShooterMotor();
  
    public abstract boolean isClimberReady();

    public abstract CANMotorControllerInfo getClimberMotor();
  
    public abstract boolean isShooterFeederReady();
  
    public abstract CANMotorControllerInfo getShooterFeederMotor();
  
    public abstract boolean isFuelIntakeMotorReady();
  
    public abstract CANMotorControllerInfo getFuelIntakeMotor();
}
