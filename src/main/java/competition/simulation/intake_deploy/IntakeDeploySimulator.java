package competition.simulation.intake_deploy;

import competition.Robot;
import competition.subsystems.intake_deploy.IntakeDeploySubsystem;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.system.plant.LinearSystemId;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.simulation.DCMotorSim;
import edu.wpi.first.wpilibj.simulation.ElevatorSim;
import org.littletonrobotics.junction.Logger;
import xbot.common.advantage.AKitLogger;
import xbot.common.controls.actuators.mock_adapters.MockCANMotorController;
import xbot.common.math.PIDManager;
import xbot.common.properties.PropertyFactory;
import xbot.common.simulation.MotorInternalPIDHelper;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.Degree;
import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.Meters;
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

    @Inject
    public IntakeDeploySimulator(IntakeDeploySubsystem intakeDeploy, PIDManager.PIDManagerFactory pidManagerFactory,
                                 PropertyFactory pf) {
        pf.setPrefix("Simulator/");
        aKitLog = new AKitLogger(pf.getPrefix());
        this.intakeDeploy = intakeDeploy;
        this.motor = (MockCANMotorController) intakeDeploy.intakeDeployMotor;
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
                        0.005,
                        60
                ),
                intakeDeployGearBox
        );
    }

    public Angle getAngularPosition() {
        return this.motorSim.getAngularPosition();
    }

    public boolean isDeployed() {
        // TODO: Extract
        return getAngularPosition().isNear(Degrees.of(80), Degrees.of(3));
    }

    public void update() {
        MotorInternalPIDHelper.updateInternalPID(motor, pidManager);
        if (DriverStation.isEnabled()) {
            this.motorSim.setInputVoltage(this.motor.getPower() * RobotController.getBatteryVoltage());
        } else {
            this.motorSim.setInputVoltage(0);
        }
        this.motorSim.update(Robot.LOOP_INTERVAL);

        var prevPosition = this.motor.getPosition();
        this.motor.setPosition(getAngularPosition());
        this.motor.setVelocity(prevPosition.minus(this.motor.getPosition()).per(Second).times(Robot.LOOP_INTERVAL));

        aKitLog.record("Intake Deployed", isDeployed());
    }
}
