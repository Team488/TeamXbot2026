# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Team 488 FRC robot code for the 2026 season. Built with Java, WPILib, and Dagger 2 dependency injection. Supports multiple robot configurations (2023, 2025, 2026, Robox practice bot) through an electrical contract system.

## Build Commands

### Basic Development
```bash
./gradlew build              # Build and run tests
./gradlew deploy             # Deploy to robot (roboRIO-488-frc.local)
./gradlew simulateJava       # Run robot simulator with GUI

# Use local SeriouslyCommonLib for development (requires ../SeriouslyCommonLib)
./gradlew build -DuseLocalCommonLib=true
```

### Testing
```bash
./gradlew test               # Run all unit tests
./gradlew jacocoTestReport   # Generate code coverage report
./gradlew checkstyle         # Run checkstyle linter
```

### AdvantageKit
```bash
./gradlew replayWatch        # Watch and replay logged robot data
```

### Custom Tasks
```bash
./gradlew copyResources      # Deploy log4j config to robot via SSH
```

## Architecture Overview

### Dependency Injection (Dagger 2)

The codebase uses **Dagger 2** for compile-time dependency injection. All robot configurations are defined through DI components and modules.

**Component Selection** (`Robot.createDaggerComponent()`):
- Runtime selection via `Preferences.getString("ContractToUse", "Competition")`
- Options: "Competition" (2026), "2025", "2023", "Robox", or auto-selects `SimulationComponent` in simulation
- Each component wires a different `ElectricalContract` implementation

**Key Modules**:
- `CompetitionModule` - 2026 competition bindings (uses `Contract2026`)
- `Module2025` / `Module2023` - Year-specific bindings
- `RoboxModule` - Practice robot bindings
- `CommonModule` - Shared bindings (swerve modules, field layout, vision)
- `SimulatedRobotModule` - Simulation-specific bindings
- From SeriouslyCommonLib: `RobotModule`, `RealDevicesModule`, `RealControlsModule`

### Electrical Contract System

**Purpose**: Decouple hardware wiring from subsystem logic. Change entire robot configurations without touching subsystem code.

**Hierarchy**:
```
ElectricalContract (abstract base)
├── Contract2026 (current year, most complete)
├── Contract2025 extends Contract2026 (overrides specific CAN IDs)
├── Contract2023 (legacy configuration)
├── RoboxContract (practice robot)
└── UnitTestCompetitionContract (unit testing)
```

**Pattern**: Each contract defines:
- `isXxxReady()` - Whether a subsystem/device is available
- `getXxxMotor()` - Device information (CAN ID, bus, inversion, etc.)
- Camera calibration, encoder positions, PID constants

**Usage in Subsystems**:
```java
if (electricalContract.isLeftShooterReady()) {
    leftShooterMotor = factory.create(...);
} else {
    leftShooterMotor = null;  // Subsystem gracefully handles missing hardware
}
```

### Subsystem Structure

Each subsystem follows this pattern:
```
subsystems/subsystem_name/
├── XxxSubsystem.java                    # Main subsystem class
└── commands/
    ├── XxxOutputCommand.java            # Primary action
    ├── XxxStopCommand.java              # Default stop command
    └── XxxMaintainerCommand.java        # Background maintenance (optional)
```

**Base Classes**:
- `BaseSubsystem` - From SeriouslyCommonLib, provides logging, properties, AdvantageKit integration
- `BaseSetpointSubsystem<TUnits, TRawUnits>` - For subsystems with setpoint control (e.g., shooter velocity)

**Command Registration**:
- Default commands: Set in `SubsystemDefaultCommandMap`
- Operator bindings: Set in `OperatorCommandMap`
- Both injected and called in `Robot.initializeSystems()`

### SeriouslyCommonLib

Team 488's shared library, available from Maven at `xbot.common:SeriouslyCommonLib:20250220.1`.

**Composite Build Support**: By default, uses Maven. To test local library changes, enable composite build:
- Use local: `./gradlew build -DuseLocalCommonLib=true`
- Use Maven (default): `./gradlew build`
- Environment variable: `export USE_LOCAL_COMMON_LIB=true`

When working on SeriouslyCommonLib:
1. Clone both repos side-by-side: `git clone <url> ../SeriouslyCommonLib`
2. Build with flag: `./gradlew build -DuseLocalCommonLib=true`
3. Changes to SeriouslyCommonLib are rebuilt automatically

The library provides:

**Core Framework**:
- `BaseRobot` - Main robot loop, handles initialization and periodic updates
- `BaseSubsystem` / `BaseSetpointSubsystem` - Subsystem base classes with logging
- `BaseMaintainerCommand` - Maintains subsystem state with human override via `HumanVsMachineDecider`

**Hardware Abstraction**:
- `XCANMotorController` - Unified motor interface (supports CTRE TalonFX, SparkMax)
- PID control, current limiting, gear ratios, inversion all configured declaratively
- AdvantageKit auto-logging integration via `DataFrameRefreshable`

**Control Systems**:
- `HumanVsMachineDecider` - Blends human input with automated control
- `PIDManager` - Tunable PID controllers
- `XXboxController` - Xbox gamepad with dead zones and utilities

**Vision/Localization**:
- `AprilTagVisionSubsystem` - AprilTag detection and pose estimation
- `BasePoseSubsystem` - Odometry + vision fusion for robot localization

**Properties System**:
- `PropertyFactory` - Creates runtime-tunable parameters that persist across reboots
- Property types: `DoubleProperty`, `IntProperty`, `BooleanProperty`, etc.
- Automatically synced to NetworkTables and RoboRIO filesystem

### AdvantageKit Integration

**Purpose**: Comprehensive logging and replay for debugging robot behavior.

**Integration**:
1. Subsystems implement `DataFrameRefreshable` and are registered in `Robot.initializeSystems()`
2. Motor controllers and sensors auto-log via `@AutoLog` annotation processor
3. All logged data can be replayed with `./gradlew replayWatch`

**Logged Data**: Motor voltages/currents/positions, PID states, vision targets, odometry, subsystem setpoints

## Key Subsystems

### DriveSubsystem
- Swerve drive with 4 modules (FrontLeft, FrontRight, RearLeft, RearRight)
- Injected via `@FrontLeftDrive`, `@FrontRightDrive`, etc. qualifiers
- Supports field-oriented drive, heading targeting, and look-at-point control
- Default command: `SwerveDriveWithJoysticksCommand`

### ShooterSubsystem
- Multi-wheel shooter (left, middle, right motors)
- Uses `BaseSetpointSubsystem<AngularVelocity, Double>` for velocity control
- `ShooterWheelMaintainerCommand` maintains target RPM with human override support
- Velocity trim commands available for fine-tuning

### PoseSubsystem
- Fuses swerve odometry with AprilTag vision
- Provides robot field position for autonomous and driver assistance

### Vision (AprilTagVisionSubsystemExtended)
- Extends `AprilTagVisionSubsystem` from SeriouslyCommonLib
- Detects AprilTags for pose estimation
- Camera configuration defined in electrical contracts

## Development Patterns

### Adding a New Subsystem
1. Create subsystem class extending `BaseSubsystem` or `BaseSetpointSubsystem`
2. Add device availability methods to `ElectricalContract` (e.g., `isNewSubsystemReady()`)
3. Implement contract methods in `Contract2026` (and other contracts as needed)
4. Create commands in `subsystems/new_subsystem/commands/`
5. Register subsystem in appropriate DI module (usually `CommonModule`)
6. Set default command in `SubsystemDefaultCommandMap`
7. Bind operator controls in `OperatorCommandMap`
8. Add to AdvantageKit logging in `Robot.initializeSystems()` if needed

### Changing Robot Configuration
- Modify the `ContractToUse` preference on the robot (via NetworkTables or Driver Station)
- Options: "Competition", "2025", "2023", "Robox"
- Robot will use the selected contract on next boot

### Unit Testing
- Use `UnitTestCompetitionContract` for tests - it provides mock electrical configuration
- `DaggerCompetitionTestComponent` wires up the test DI graph
- Tests run on any PC/CI server without robot hardware

## Important Notes

- **Main class**: `competition.Main` (defined in `build.gradle`)
- **Team number**: Loaded from `.wpilib/wpilib_preferences.json`
- **Checkstyle**: Requires local `../SeriouslyCommonLib` directory (uses `xbotcheckstyle.xml`)
- **Loop interval**: 20ms (defined in `Robot.LOOP_INTERVAL`)
- **Log4j config**: Deployed to robot via `copyResources` task (requires local `../SeriouslyCommonLib/lib/log4jConfig/log4j.xml`)

### For Students (Simple Setup)
Just clone this repo and run `./gradlew build`. SeriouslyCommonLib is fetched from Maven automatically.

### For Developers (Library Development)
Clone both repos side-by-side to enable local library development:
```bash
git clone <this-repo-url> TeamXbot2026
git clone <library-url> SeriouslyCommonLib
cd TeamXbot2026
./gradlew build -DuseLocalCommonLib=true  # Uses local SeriouslyCommonLib
```
