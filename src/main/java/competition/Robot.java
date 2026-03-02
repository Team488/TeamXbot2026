
package competition;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.commands.PathfindingCommand;
import com.pathplanner.lib.config.ModuleConfig;
import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.controllers.PPHolonomicDriveController;
import com.pathplanner.lib.controllers.PathFollowingController;
import com.pathplanner.lib.config.PIDConstants;
import competition.electrical_contract.ElectricalContract;
import competition.injection.components.BaseRobotComponent;
import competition.injection.components.DaggerRobotComponent;
import competition.injection.components.DaggerRobotComponent2023;
import competition.injection.components.DaggerRobotComponent2025;
import competition.injection.components.DaggerRoboxComponent;
import competition.injection.components.DaggerSimulationComponent;
import competition.simulation.BaseSimulator;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import competition.subsystems.shooter.ShooterSubsystem;
import competition.subsystems.voltage_alert.VoltageMonitorSubsystem;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.units.Units;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xbot.common.command.BaseRobot;
import xbot.common.injection.electrical_contract.CANMotorControllerInfo;
import xbot.common.injection.electrical_contract.MotorControllerType;
import xbot.common.injection.electrical_contract.TalonFxMotorControllerOutputConfig;
import xbot.common.injection.swerve.SwerveInstance;
import xbot.common.math.FieldPose;
import xbot.common.subsystems.pose.BasePoseSubsystem;

public class Robot extends BaseRobot {
    Logger log = LogManager.getLogger(Robot.class);

    public static final double LOOP_INTERVAL = 0.02;

    BaseSimulator simulator;

    Robot() {
        super(LOOP_INTERVAL);
    }

    @Override
    protected void initializeSystems() {
        super.initializeSystems();
        getInjectorComponent().subsystemDefaultCommandMap();
        getInjectorComponent().operatorCommandMap();
        getInjectorComponent().swerveDefaultCommandMap();
        getInjectorComponent().shooterSubsystem();
        getInjectorComponent().lightsSubsystem();
        getInjectorComponent().hopperRollerSubsystem();
        getInjectorComponent().intakeDeploySubsystem();
        getInjectorComponent().voltageMonitorSubsystem();

        if (BaseRobot.isSimulation()) {
            simulator = getInjectorComponent().simulator();
        }

        dataFrameRefreshables.add((DriveSubsystem)getInjectorComponent().driveSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().poseSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().aprilTagVisionSubsystemExtended());
        dataFrameRefreshables.add(getInjectorComponent().shooterSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().shooterFeederSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().hoodSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().intakeSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().intakeDeploySubsystem());
        dataFrameRefreshables.add(getInjectorComponent().lightsSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().hopperRollerSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().voltageMonitorSubsystem());

        // Configure PathPlanner AutoBuilder for pathfinding
        configurePathPlanner();
    }

    private void configurePathPlanner() {
        DriveSubsystem drive = (DriveSubsystem) getInjectorComponent().driveSubsystem();
        PoseSubsystem pose = (PoseSubsystem) getInjectorComponent().poseSubsystem();
        ElectricalContract contract = getInjectorComponent().electricalContract();

        // Get swerve module positions from the electrical contract
        Translation2d frontLeftOffset = contract.getSwerveModuleOffsets(new SwerveInstance("FrontLeftDrive"));
        Translation2d frontRightOffset = contract.getSwerveModuleOffsets(new SwerveInstance("FrontRightDrive"));
        Translation2d rearLeftOffset = contract.getSwerveModuleOffsets(new SwerveInstance("RearLeftDrive"));
        Translation2d rearRightOffset = contract.getSwerveModuleOffsets(new SwerveInstance("RearRightDrive"));

        // Get wheel radius from the contract's wheel diameter
        double wheelRadiusMeters = contract.getDriveWheelDiameter().in(Units.Meters) / 2.0;

        // Get gear ratio from the contract
        double driveGearRatio = contract.getDriveGearRatio();

        // Get stator current limit from the contract's drive motor configuration
        CANMotorControllerInfo driveMotorInfo = contract.getDriveMotor(new SwerveInstance("FrontLeftDrive"));
        double statorCurrentLimitAmps = 60.0; // fallback
        if (driveMotorInfo.outputConfig() instanceof TalonFxMotorControllerOutputConfig talonConfig) {
            statorCurrentLimitAmps = talonConfig.statorCurrentLimit.in(Units.Amps);
        }

        // Map motor controller type from the contract to a DCMotor model
        DCMotor driveMotor;
        if (driveMotorInfo.type() == MotorControllerType.TalonFx) {
            driveMotor = DCMotor.getKrakenX60(1);
        } else {
            driveMotor = DCMotor.getNEO(1);
        }

        ModuleConfig moduleConfig = new ModuleConfig(
                wheelRadiusMeters,
                4.5,             // max drive velocity m/s
                1.2,             // wheel coefficient of friction (not in contract)
                driveMotor.withReduction(driveGearRatio),
                statorCurrentLimitAmps,
                1                // 1 motor per module
        );

        RobotConfig robotConfig = new RobotConfig(
                45.0,            // robot mass in kg (not in contract)
                6.8,             // moment of inertia in kg*m^2 (not in contract)
                moduleConfig,
                frontLeftOffset,
                frontRightOffset,
                rearLeftOffset,
                rearRightOffset
        );

        PathFollowingController controller = new PPHolonomicDriveController(
                new PIDConstants(5.0, 0.0, 0.0),  // translation PID
                new PIDConstants(5.0, 0.0, 0.0)   // rotation PID
        );

        AutoBuilder.configure(
                pose::getCurrentPose2d,                    // Pose supplier
                pose::setCurrentPosition,                  // Pose reset consumer
                drive::getRobotRelativeSpeeds,             // Robot-relative speeds supplier
                (speeds, feedforwards) -> drive.driveWithChassisSpeeds(speeds),  // Drive output
                controller,
                robotConfig,
                () -> {
                    // Flip path if we are on the red alliance
                    var alliance = DriverStation.getAlliance();
                    return alliance.isPresent() && alliance.get() == DriverStation.Alliance.Red;
                },
                drive                                      // Drive subsystem requirement
        );

        // Warm up the pathfinding command so first usage isn't slow
        CommandScheduler.getInstance().schedule(PathfindingCommand.warmupCommand());
        log.info("PathPlanner AutoBuilder configured and warmup scheduled.");
    }

    protected BaseRobotComponent createDaggerComponent() {
        if (BaseRobot.isReal()) {
            // Initialize the contract to use if this is a fresh robot. Assume competition since that's the safest.
            if (!Preferences.containsKey("ContractToUse")) {
                Preferences.setString("ContractToUse", "Competition");
            }

            String chosenContract = Preferences.getString("ContractToUse", "Competition");

            switch (chosenContract) {
                case "2023":
                    log.info("Using 2023 contract");
                    return DaggerRobotComponent2023.create();
                case "2025":
                    log.info("Using 2025 contract");
                    return DaggerRobotComponent2025.create();
                case "Robox":
                    System.out.println("Using Robox contract");
                    return DaggerRoboxComponent.create();
                default:
                    if(!Preferences.containsKey("ContractToUse")) {
                        Preferences.setString("ContractToUse", "Competition");
                    }
                    log.info("Using Competition contract");
                    return DaggerRobotComponent.create();
            }
        } else {
            return DaggerSimulationComponent.create();
        }
    }

    public BaseRobotComponent getInjectorComponent() {
        return (BaseRobotComponent)super.getInjectorComponent();
    }

    @Override
    public void simulationInit() {
        super.simulationInit();
        // Automatically enables the robot; remove this line of code if you want the robot
        // to start in a disabled state (as it would on the field). However, this does save you the 
        // hassle of navigating to the DS window and re-enabling the simulated robot.
        DriverStationSim.setEnabled(true);
        //webots.setFieldPoseOffset(getFieldOrigin());
    }

    private FieldPose getFieldOrigin() {
        // Modify this to whatever the simulator coordinates are for the "FRC origin" of the field.
        // From a birds-eye view where your alliance station is at the bottom, this is the bottom-left corner
        // of the field.
        return new FieldPose(
            -2.33*PoseSubsystem.INCHES_IN_A_METER, 
            -4.58*PoseSubsystem.INCHES_IN_A_METER, 
            BasePoseSubsystem.FACING_TOWARDS_DRIVERS
            );
    }

    @Override
    public void simulationPeriodic() {
        super.simulationPeriodic();

        if (simulator != null) {
            simulator.update();
        }
    }
}
