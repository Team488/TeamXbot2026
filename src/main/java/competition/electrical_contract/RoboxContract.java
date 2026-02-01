package competition.electrical_contract;

import xbot.common.injection.electrical_contract.CANBusId;
import xbot.common.injection.electrical_contract.CANMotorControllerInfo;
import xbot.common.injection.electrical_contract.CANMotorControllerOutputConfig;
import xbot.common.injection.electrical_contract.MotorControllerType;

import javax.inject.Inject;

import static edu.wpi.first.units.Units.Amps;

public class RoboxContract extends Contract2026 {
    @Inject
    public RoboxContract() {}

    @Override
    public boolean isDriveReady() { return false; }

    @Override
    public boolean areCanCodersReady() { return false; }

    public boolean isShooterReady() { return true; }

    public CANMotorControllerInfo getShooterMotor() {
        return new CANMotorControllerInfo("ShooterMotor",
                MotorControllerType.SparkMax,
                CANBusId.RIO,
                32,
                new CANMotorControllerOutputConfig().withStatorCurrentLimit(Amps.of(10)));
    }
}
