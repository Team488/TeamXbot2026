package competition.subsystems.pose;

import edu.wpi.first.units.Units;
import xbot.common.controls.io_inputs.XGyroIoInputs;
import xbot.common.controls.sensors.XGyro;
import xbot.common.injection.electrical_contract.IMUInfo;

public class XbotIMUToXGyroAdapter extends XGyro {

    private final XbotIMU imu;

    public XbotIMUToXGyroAdapter(XbotIMU imu) {
        // Create a mock IMUInfo so XGyro can initialize safely
        super(IMUInfo.createMock(new IMUInfo(XGyro.InterfaceType.spi)));
        this.imu = imu;
    }

    @Override
    public boolean isBroken() {
        // SystemCore IMU never reports broken
        return false;
    }

    @Override
    protected void updateInputs(XGyroIoInputs inputs) {
        // Feed IMU values into the XGyro input structure
        inputs.yaw = Units.Degrees.of(imu.getRotation().getDegrees());
        inputs.pitch = Units.Degrees.of(imu.getPitch());
        inputs.roll = Units.Degrees.of(imu.getRoll());
        inputs.yawAngularVelocity = Units.DegreesPerSecond.of(imu.getAngularVelocity());

        // Mark IMU as connected
        inputs.isConnected = true;
    }

    @Override
    public void close() {
        // Nothing to close for SystemCore IMU
    }
}
