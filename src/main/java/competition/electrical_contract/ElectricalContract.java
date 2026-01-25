package competition.electrical_contract;

import edu.wpi.first.math.geometry.Translation2d;
import xbot.common.injection.electrical_contract.CANBusId;
import xbot.common.injection.electrical_contract.CANLightControllerInfo;
import xbot.common.injection.electrical_contract.CANMotorControllerInfo;
import xbot.common.injection.electrical_contract.DeviceInfo;
import xbot.common.injection.electrical_contract.XCameraElectricalContract;
import xbot.common.injection.electrical_contract.XSwerveDriveElectricalContract;
import xbot.common.injection.swerve.SwerveInstance;

public abstract class ElectricalContract implements XSwerveDriveElectricalContract, XCameraElectricalContract {
    public boolean isDriveReady() {
        return false;
    }

    public boolean areCanCodersReady() {
        return false;
    }

    public boolean isHoodReady() {
        return false;
    }

    public boolean isIntakeDeployReady() {
        return false;
    }

    public boolean isClimberReady() {
        return false;
    }

    public boolean isShooterReady() {
        return false;
    }

    public boolean isShooterFeederReady() {
        return false;
    }

    public boolean isFuelIntakeMotorReady() {
        return false;
    }

    public abstract CANMotorControllerInfo getDriveMotor(SwerveInstance swerveInstance);

    public abstract CANMotorControllerInfo getSteeringMotor(SwerveInstance swerveInstance);

    public abstract DeviceInfo getSteeringEncoder(SwerveInstance swerveInstance);

    public abstract Translation2d getSwerveModuleOffsets(SwerveInstance swerveInstance);

    public abstract CANMotorControllerInfo getHoodMotor();

    public abstract CANMotorControllerInfo getIntakeDeployMotor();

    public abstract CANMotorControllerInfo getShooterMotor();

    public abstract CANMotorControllerInfo getClimberMotor();

    public abstract CANMotorControllerInfo getShooterFeederMotor();

    public abstract CANMotorControllerInfo getFuelIntakeMotor();
  
    public abstract CANLightControllerInfo getLightControlerInfo();
}
