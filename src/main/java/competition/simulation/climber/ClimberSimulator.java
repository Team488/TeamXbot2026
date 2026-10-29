package competition.simulation.climber;

import competition.Robot;
import competition.simulation.SimulatorConstants;
import competition.subsystems.climber.ClimberSubsystem;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.system.plant.LinearSystemId;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.MockDigitalInput;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.simulation.DCMotorSim;
import xbot.common.advantage.AKitLogger;
import xbot.common.controls.actuators.mock_adapters.MockCANMotorController;
import xbot.common.math.PIDManager;
import xbot.common.properties.PropertyFactory;
import xbot.common.simulation.MotorInternalPIDHelper;

import javax.inject.Inject;
import javax.inject.Singleton;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.Rotations;

@Singleton
public class ClimberSimulator {
    final AKitLogger aKitLog;
    final DCMotorSim motorSim;

    // We have 2 motors for climber but don't think it really matters for this case
    final DCMotor climberGearBox = DCMotor.getKrakenX60(1);
    final PIDManager pidManager;
    final ClimberSubsystem climber;
    final MockCANMotorController motor;
    final MockDigitalInput homedSensor;

    @Inject
    public ClimberSimulator(ClimberSubsystem climber, PIDManager.PIDManagerFactory pidManagerFactory,
                            PropertyFactory pf) {
        pf.setPrefix("Simulator/");
        aKitLog = new AKitLogger(pf.getPrefix());
        this.climber = climber;
        this.motor = (MockCANMotorController) climber.climberMotorLeft;
        this.homedSensor = (MockDigitalInput) climber.climberSensor;
        this.pidManager = pidManagerFactory.create(
                pf.getPrefix() + "ClimberSimulatorPID",
                0.2,
                0.001,
                0.0,
                0.0,
                1.0,
                -1.0
        ); // TODO: Adjust

        this.motorSim = new DCMotorSim(
                LinearSystemId.createDCMotorSystem(
                        climberGearBox,
                        SimulatorConstants.intakeDeployJKgMetersSquared,
                        SimulatorConstants.intakeDeployGearing
                ),
                climberGearBox
        );
    }

    public Angle getAngularPosition() {
        return this.motorSim.getAngularPosition();
    }

    public boolean isHomed() {
        return getAngularPosition()
                .isNear(climber.retractedAngle.get(), Degrees.of(5));
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
        double motorRotation = mechanismAngle.in(Degrees) / climber.mechanismDegreesPerMotorRotation.get();
        this.motor.setPosition(Rotations.of(motorRotation));
        if (homedSensor != null) {
            homedSensor.setValue(isHomed());
        }
    }
}
