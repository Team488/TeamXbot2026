package competition.simulation.intake_deploy;

import competition.Robot;
import competition.simulation.SimulatorConstants;
import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.system.plant.LinearSystemId;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.simulation.DCMotorSim;
import xbot.common.advantage.AKitLogger;
import xbot.common.controls.actuators.mock_adapters.MockCANMotorController;
import xbot.common.controls.sensors.mock_adapters.MockAbsoluteEncoder;
import xbot.common.math.PIDManager;
import xbot.common.properties.DoubleProperty;
import xbot.common.properties.PropertyFactory;
import xbot.common.simulation.MotorInternalPIDHelper;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.DegreesPerSecond;
import static edu.wpi.first.units.Units.Radians;
import static edu.wpi.first.units.Units.Rotations;
import static edu.wpi.first.units.Units.Second;

@Singleton
public class IntakeDeploySimulator {
    final AKitLogger aKitLog;

    final DCMotorSim motorSim;
    final DCMotor intakeDeployGearBox = DCMotor.getKrakenX60(1);
    final PIDManager pidManager;

    final IntakeDeploySubsystem intakeDeploy;
    final MockCANMotorController motor;
    final MockAbsoluteEncoder absoluteEncoder;

    @Inject
    public IntakeDeploySimulator(IntakeDeploySubsystem intakeDeploy, PIDManager.PIDManagerFactory pidManagerFactory,
                                 PropertyFactory pf) {
        pf.setPrefix("Simulator/");
        aKitLog = new AKitLogger(pf.getPrefix());
        this.intakeDeploy = intakeDeploy;
        this.motor = (MockCANMotorController) intakeDeploy.intakeDeployMotor;
        this.absoluteEncoder = (MockAbsoluteEncoder) intakeDeploy.intakeDeployAbsoluteEncoder;
        this.pidManager = pidManagerFactory.create(
                pf.getPrefix() + "IntakeDeploySimulatorPID",
                0.2,
                0.001,
                0.0,
                0.0,
                1.0,
                -1.0
        ); // TODO: Adjust

        this.motorSim = new DCMotorSim(
                LinearSystemId.createDCMotorSystem(
                        intakeDeployGearBox,
                        SimulatorConstants.intakeDeployJKgMetersSquared,
                        SimulatorConstants.intakeDeployGearing
                ),
                intakeDeployGearBox
        );
    }

    public Angle getAngularPosition() {
        return this.motorSim.getAngularPosition();
    }

    public boolean isDeployed() {
        return getAngularPosition().isNear(SimulatorConstants.intakeDeployedAngle, Degrees.of(5));
    }

    public void update() {
        // Update motor power
        MotorInternalPIDHelper.updateInternalPID(motor, pidManager);

        // Update the internal motorSim using our motor power
        if (DriverStation.isEnabled()) {
            this.motorSim.setInputVoltage(this.motor.getPower() * RobotController.getBatteryVoltage());
        } else {
            this.motorSim.setInputVoltage(0);
        }
        this.motorSim.update(Robot.LOOP_INTERVAL);

        // Update motor position and velocity with the motorSim
        var mechanismAngle = getAngularPosition();
        double motorRotation = mechanismAngle.in(Degrees) / intakeDeploy.degreesPerRotation.get();

        this.motor.setPosition(Rotations.of(motorRotation));

        if (absoluteEncoder != null) {
            absoluteEncoder.setPosition(mechanismAngle);
            aKitLog.record("IntakeDeployEncoderPos", this.absoluteEncoder.getPosition().in(Degrees));
        }

        aKitLog.record("IntakeDeployMotorPos", this.motor.getPosition().in(Rotations));
        aKitLog.record("IntakeDeployed", isDeployed());
    }
}
