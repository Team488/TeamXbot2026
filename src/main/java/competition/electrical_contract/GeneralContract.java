package competition.electrical_contract;

import xbot.common.injection.electrical_contract.CANBusId;
import xbot.common.injection.electrical_contract.CANLightControllerInfo;
import xbot.common.injection.electrical_contract.CANLightControllerOutputConfig;
import xbot.common.injection.electrical_contract.CANMotorControllerInfo;
import xbot.common.injection.electrical_contract.CANMotorControllerOutputConfig;
import xbot.common.injection.electrical_contract.DeviceInfo;
import xbot.common.injection.electrical_contract.LEDStripType;
import xbot.common.injection.electrical_contract.LightControllerType;
import xbot.common.injection.electrical_contract.MotorControllerType;
import xbot.common.injection.electrical_contract.PDHPort;
import xbot.common.injection.electrical_contract.PowerSource;
import xbot.common.injection.electrical_contract.TalonFxMotorControllerOutputConfig;

import java.util.Set;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Seconds;

public abstract class GeneralContract extends ElectricalContract {

    protected GeneralContract(Set<Hardware> readinessSet) {
        super(readinessSet);
    }

    @Override
    public DeviceInfo getHoodServoLeft() {
        return new DeviceInfo("HoodServoLeft", 7);
    }

    @Override
    public DeviceInfo getHoodServoRight() {
        return new DeviceInfo("HoodServoRight", 9);
    }

    @Override
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
    public CANMotorControllerInfo getIntakeDeployMotor() {
        return new CANMotorControllerInfo("IntakeDeployMotor",
                MotorControllerType.TalonFx,
                CANBusId.Canivore,
                34,
                PDHPort.PDH14,
                new TalonFxMotorControllerOutputConfig()
                        .withSupplyCurrentLimit(Amps.of(15), Amps.of(30), Seconds.of(1))
                        .withStatorCurrentLimit(Amps.of(50))
                        .withRemoteCanCoderFeedback(getIntakeDeployAbsoluteEncoder().channel));
    }

    @Override
    public DeviceInfo getIntakeDeployAbsoluteEncoder() {
        return new DeviceInfo("IntakeDeployAbsoluteEncoderReady", CANBusId.Canivore, 58);
    }

    @Override
    public CANMotorControllerInfo getFuelIntakeMotor() {
        return new CANMotorControllerInfo("FuelIntakeMotor",
                MotorControllerType.TalonFx,
                CANBusId.Canivore,
                32,
                PDHPort.PDH12,
                new TalonFxMotorControllerOutputConfig()
                        .withInversionType(CANMotorControllerOutputConfig.InversionType.Inverted)
                        .withStatorCurrentLimit(Amps.of(70)));
    }

    @Override
    public CANLightControllerInfo getLightControllerInfo() {
        return new CANLightControllerInfo("Lights",
                LightControllerType.Candle, CANBusId.Canivore,
                57, new CANLightControllerOutputConfig(LEDStripType.GRB,
                0.15, new int[] {8, 30, 30}));

    }

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
    public DeviceInfo getClimberSensor() {
        return new DeviceInfo("ClimberSensor", 0, PowerSource.RIO);
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
}
