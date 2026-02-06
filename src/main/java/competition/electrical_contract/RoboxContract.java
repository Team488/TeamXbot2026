package competition.electrical_contract;

import xbot.common.injection.electrical_contract.CANBusId;
import xbot.common.injection.electrical_contract.CANMotorControllerInfo;
import xbot.common.injection.electrical_contract.CANMotorControllerOutputConfig;
import xbot.common.injection.electrical_contract.MotorControllerType;
import xbot.common.injection.electrical_contract.PDHPort;
import xbot.common.injection.electrical_contract.SparkMaxMotorControllerOutputConfig;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.Amps;

public class RoboxContract extends Contract2026 {
    @Inject
    public RoboxContract() {}

    @Override
    public boolean isDriveReady() { return false; }

    @Override
    public boolean areCanCodersReady() { return false; }

    @Override
    public boolean isLeftShooterReady() { return false; }

    @Override
    public boolean isMiddleShooterReady() { return false; }

    @Override
    public boolean isRightShooterReady() { return false; }

    @Override
    public CANMotorControllerInfo getLeftShooterMotor() {
        return new CANMotorControllerInfo("ShooterMotor",
                MotorControllerType.TalonFx,
                CANBusId.RIO,
                918,
                PDHPort.PDH00,
                new CANMotorControllerOutputConfig());
    }

    @Override
    public CANMotorControllerInfo getMiddleShooterMotor() {
        return new CANMotorControllerInfo("ShooterMotor",
                MotorControllerType.TalonFx,
                CANBusId.RIO,
                211,
                new CANMotorControllerOutputConfig());
    }

    @Override
    public CANMotorControllerInfo getRightShooterMotor() {
        return new CANMotorControllerInfo("ShooterMotor",
                MotorControllerType.TalonFx,
                CANBusId.RIO,
                900,
                new CANMotorControllerOutputConfig());
    }
}
