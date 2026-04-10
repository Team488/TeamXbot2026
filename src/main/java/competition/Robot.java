
package competition;

import com.pathplanner.lib.commands.PathPlannerAuto;
import competition.auto_programs.CollectAndShootTwiceCommand;
import competition.auto_programs.ShootFromHubCommandGroup;
import competition.auto_programs.ShootFromTrenchCommandGroup;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import competition.injection.components.BaseRobotComponent;
import competition.injection.components.DaggerRobotComponent;
import competition.injection.components.DaggerRobotComponent2023;
import competition.injection.components.DaggerRobotComponent2025;
import competition.injection.components.DaggerRoboxComponent;
import competition.injection.components.DaggerSimulationComponent;
import competition.operator_interface.OperatorInterface;
import competition.simulation.BaseSimulator;
import competition.subsystems.drive.DriveSubsystem;
import competition.subsystems.pose.PoseSubsystem;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import xbot.common.command.BaseRobot;
import xbot.common.math.FieldPose;
import xbot.common.subsystems.pose.BasePoseSubsystem;

public class Robot extends BaseRobot {
    Logger log = LogManager.getLogger(Robot.class);

    public static final double LOOP_INTERVAL = 0.04;
    private final Field2d field = new Field2d();


    BaseSimulator simulator;
    OperatorInterface oi;

    Robot() {
        super(LOOP_INTERVAL);
    }

    @Override
    protected void initializeSystems() {
        super.initializeSystems();
        getInjectorComponent().configurePathPlannerLib();
        getInjectorComponent().subsystemDefaultCommandMap();
        getInjectorComponent().operatorCommandMap();
        getInjectorComponent().swerveDefaultCommandMap();
        getInjectorComponent().shooterSubsystem();
        getInjectorComponent().lightsSubsystem();
        getInjectorComponent().hopperRollerSubsystem();
        getInjectorComponent().intakeDeploySubsystem();
        getInjectorComponent().voltageMonitorSubsystem();
        getInjectorComponent().climberSubsystem();



        if (BaseRobot.isSimulation()) {
            simulator = getInjectorComponent().simulator();
        }

        oi = getInjectorComponent().operatorInterface();
        autonomousCommandSelector.setCurrentAutonomousCommand(getInjectorComponent().shootFromTrenchCommandGroup());

        dataFrameRefreshables.add((DriveSubsystem)getInjectorComponent().driveSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().poseSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().aprilTagVisionSubsystemExtended());
        dataFrameRefreshables.add(getInjectorComponent().shooterSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().shooterFeederSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().hoodSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().collectorSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().intakeDeploySubsystem());
        dataFrameRefreshables.add(getInjectorComponent().lightsSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().hopperRollerSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().voltageMonitorSubsystem());
        dataFrameRefreshables.add(getInjectorComponent().climberSubsystem());

        getInjectorComponent().superstructureMechanismSubsystem();

        CommandScheduler.getInstance().schedule(getInjectorComponent().gamepadRumbleCommand());

        SmartDashboard.putData("Field", field);

        CommandScheduler.getInstance().schedule(getInjectorComponent().gamepadRumbleCommand());
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
    public void autonomousInit() {
        super.autonomousInit();
        var pose = (PoseSubsystem) getInjectorComponent().poseSubsystem();
        CommandScheduler.getInstance().schedule(pose.getResetTranslationToVisionEstimateCommand());
        if (autonomousCommand != null) {
            CommandScheduler.getInstance().schedule(autonomousCommand);
        }

        if(BaseRobot.isSimulation()) {
            getInjectorComponent().simulator().resetForAuto();
        }
    }

    @Override
    public void teleopInit() {
        super.teleopInit();
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

    @Override
    protected void sharedPeriodic() {
        super.sharedPeriodic();

        if (this.oi != null) {
            this.oi.periodic();
        }

        String matchShift;

        if (DriverStation.getMatchTime() <= 140 && DriverStation.getMatchTime() >= 130) {
            matchShift = "Transition Shift";
        } else if (DriverStation.getMatchTime() <= 130 && DriverStation.getMatchTime() >= 105) {
            matchShift = "Shift 1";
        } else if (DriverStation.getMatchTime() <= 105 && DriverStation.getMatchTime() >= 80) {
            matchShift = "Shift 2";
        } else if (DriverStation.getMatchTime() <= 80 && DriverStation.getMatchTime() >= 55) {
            matchShift = "Shift 3";
        } else if (DriverStation.getMatchTime() <= 55 && DriverStation.getMatchTime() >= 30) {
            matchShift = "Shift 4";
        } else if (DriverStation.getMatchTime() <= 30 && DriverStation.getMatchTime() >= 0) {
            matchShift = "Endgame";
        } else {
            matchShift = "Unknown";
        }

        PoseSubsystem pose = (PoseSubsystem) getInjectorComponent().poseSubsystem();
        field.setRobotPose(pose.getCurrentPose2d());

        SmartDashboard.putNumber("Current Match Time", DriverStation.getMatchTime());
        SmartDashboard.putString("Match Shift", matchShift);
        SmartDashboard.putBoolean("Is Hood Down", getInjectorComponent().hoodSubsystem().isHoodDown());
    }
}
