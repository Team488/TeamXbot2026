package competition.electrical_contract;

import xbot.common.injection.electrical_contract.CANBusId;
import xbot.common.injection.electrical_contract.CANMotorControllerInfo;
import xbot.common.injection.electrical_contract.CANMotorControllerOutputConfig;
import xbot.common.injection.electrical_contract.DeviceInfo;
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
    public boolean isLeftShooterReady() { return true; }

    @Override
    public CANMotorControllerInfo getLeftShooterMotor() {
        return new CANMotorControllerInfo("ShooterMotor",
                MotorControllerType.SparkMax,
                CANBusId.RIO,
                32,
                new SparkMaxMotorControllerOutputConfig().withSmartCurrentLimit(Amps.of(40)));
    }
}
