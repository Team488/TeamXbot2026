package electrical.report;

import competition.electrical_contract.Contract2026;
import xbot.common.injection.electrical_contract.CANBusId;
import xbot.common.injection.electrical_contract.CANMotorControllerInfo;
import xbot.common.injection.electrical_contract.DeviceInfo;
import xbot.common.injection.electrical_contract.IMUInfo;
import xbot.common.injection.electrical_contract.PDHPort;
import xbot.common.injection.electrical_contract.PowerSource;
import xbot.common.injection.swerve.SwerveInstance;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Electrical Contract Report Generator
 * 
 * HOW TO UPDATE CONTRACT YEAR:
 * ----------------------------
 * To switch to a new year's contract (e.g., from Contract2026 to Contract2027):
 * 
 * 1. Update the import statement at the top:
 *    Change: import competition.electrical_contract.Contract2026;
 *    To:     import competition.electrical_contract.Contract2027;
 * 
 * 2. Update the return type and instantiation in createContract() method below:
 *    Change: private static Contract2026 createContract() { return new Contract2026(); }
 *    To:     private static Contract2027 createContract() { return new Contract2027(); }
 */

public class Main {
    
    /**
     * *** CHANGE THIS WHEN UPDATING TO A NEW YEAR ***
     * This is the single point of configuration for which contract year to use.
     * Update the import statement above to match this year.
     */
    private static Contract2026 createContract() {
        return new Contract2026();
    }
    
    static class CANDevice implements Comparable<CANDevice> {
        String name;
        CANBusId busId;
        int deviceId;
        String type;
        PDHPort pdhPort;
        PowerSource powerSource;
        
        CANDevice(String name, CANBusId busId, int deviceId, String type, PDHPort pdhPort, PowerSource powerSource) {
            this.name = name;
            this.busId = busId;
            this.deviceId = deviceId;
            this.type = type;
            this.pdhPort = pdhPort;
            this.powerSource = powerSource;
        }
        
        @Override
        public int compareTo(CANDevice other) {
            return Integer.compare(this.deviceId, other.deviceId);
        }
        
        @Override
        public String toString() {
            return String.format("  ID %2d: %-40s [%s]", deviceId, name, type);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("Electrical Contract Report");
        System.out.println("=".repeat(80));
        System.out.println();
        
        var contract = createContract();
        System.out.println("Source: " + contract.getClass().getName());
        System.out.println();
        
        Map<CANBusId, List<CANDevice>> devicesByBus = new HashMap<>();
        devicesByBus.put(CANBusId.RIO, new ArrayList<>());
        devicesByBus.put(CANBusId.Canivore, new ArrayList<>());
        
        List<DeviceInfo> dioDevices = new ArrayList<>();
        List<DeviceInfo> pwmDevices = new ArrayList<>();
        Map<PowerSource, List<String>> devicesByPowerSource = new HashMap<>();
        
        // Define swerve modules array for hard-coded handling
        SwerveInstance[] swerveModules = {
            new SwerveInstance("FrontLeftDrive"),
            new SwerveInstance("FrontRightDrive"),
            new SwerveInstance("RearLeftDrive"),
            new SwerveInstance("RearRightDrive")
        };
        
        // Collect swerve drive motors and encoders (hard-coded due to parameters)
        if (contract.isDriveReady()) {
            for (SwerveInstance module : swerveModules) {
                // Drive motor
                CANMotorControllerInfo driveMotor = contract.getDriveMotor(module);
                if (driveMotor != null && driveMotor.busId() != null) {
                    devicesByBus.get(driveMotor.busId()).add(
                        new CANDevice(driveMotor.name(), driveMotor.busId(), 
                                     driveMotor.deviceId(), driveMotor.type().toString(),
                                     driveMotor.pdhPort(), null));
                    // Track power source
                    if (driveMotor.pdhPort() != null) {
                        PowerSource powerSource = PowerSource.valueOf(driveMotor.pdhPort().name());
                        devicesByPowerSource.computeIfAbsent(powerSource, k -> new ArrayList<>()).add(driveMotor.name());
                    }
                }
                
                // Steering motor
                CANMotorControllerInfo steeringMotor = contract.getSteeringMotor(module);
                if (steeringMotor != null && steeringMotor.busId() != null) {
                    devicesByBus.get(steeringMotor.busId()).add(
                        new CANDevice(steeringMotor.name(), steeringMotor.busId(), 
                                     steeringMotor.deviceId(), steeringMotor.type().toString(),
                                     steeringMotor.pdhPort(), null));
                    // Track power source
                    if (steeringMotor.pdhPort() != null) {
                        PowerSource powerSource = PowerSource.valueOf(steeringMotor.pdhPort().name());
                        devicesByPowerSource.computeIfAbsent(powerSource, k -> new ArrayList<>()).add(steeringMotor.name());
                    }
                }
                
                // Steering encoder
                DeviceInfo steeringEncoder = contract.getSteeringEncoder(module);
                if (steeringEncoder != null && steeringEncoder.canBusId != null) {
                    devicesByBus.get(steeringEncoder.canBusId).add(
                        new CANDevice(steeringEncoder.name, steeringEncoder.canBusId, 
                                     steeringEncoder.channel, "TalonFX", null, steeringEncoder.powerFrom));
                    // Track power source
                    if (steeringEncoder.powerFrom != null) {
                        devicesByPowerSource.computeIfAbsent(steeringEncoder.powerFrom, k -> new ArrayList<>()).add(steeringEncoder.name);
                    }
                }
            }
        }
        
        // Auto-discover all other devices using reflection
        autoDiscoverDevices(contract, devicesByBus, dioDevices, pwmDevices, devicesByPowerSource);
        // Print report
        for (CANBusId busId : Arrays.asList(CANBusId.RIO, CANBusId.Canivore)) {
            List<CANDevice> devices = devicesByBus.get(busId);
            Collections.sort(devices);
            
            String busName = busId.equals(CANBusId.Canivore) ? "CANBusId[id=Canivore]" : "CANBusId[id=rio]";
            System.out.println("-".repeat(80));
            System.out.println(busName + " (" + devices.size() + " devices)");
            System.out.println("-".repeat(80));
            
            if (devices.isEmpty()) {
                System.out.println("  (No devices)");
            } else {
                for (CANDevice device : devices) {
                    System.out.println(device);
                }
            }
            System.out.println();
        }
        
        System.out.println("=".repeat(80));
        System.out.println("Total CAN devices: " 
            + (devicesByBus.get(CANBusId.RIO).size() + devicesByBus.get(CANBusId.Canivore).size()));
        System.out.println("=".repeat(80));
        System.out.println();
        
        // Print DIO devices
        System.out.println("-".repeat(80));
        System.out.println("Digital I/O Devices (" + dioDevices.size() + " devices)");
        System.out.println("-".repeat(80));
        dioDevices.sort(Comparator.comparingInt(d -> d.channel));
        for (DeviceInfo device : dioDevices) {
            String powerInfo = device.powerFrom != null ? " <- " + device.powerFrom : "";
            System.out.printf("  DIO %2d: %-40s%s\n", device.channel, device.name, powerInfo);
        }
        System.out.println();
        
        // Print PWM devices
        System.out.println("-".repeat(80));
        System.out.println("PWM Devices (" + pwmDevices.size() + " devices)");
        System.out.println("-".repeat(80));
        pwmDevices.sort(Comparator.comparingInt(d -> d.channel));
        for (DeviceInfo device : pwmDevices) {
            String powerInfo = device.powerFrom != null ? " <- " + device.powerFrom : "";
            System.out.printf("  PWM %2d: %-40s%s\n", device.channel, device.name, powerInfo);
        }
        System.out.println();
        
        System.out.println("=".repeat(80));
        System.out.println("Total devices: CAN=" 
            + (devicesByBus.get(CANBusId.RIO).size() + devicesByBus.get(CANBusId.Canivore).size())
            + ", DIO=" + dioDevices.size() + ", PWM=" + pwmDevices.size());
        System.out.println("=".repeat(80));
        System.out.println();
        
        // Generate PDH Port Usage Report
        generatePDHReport(contract, swerveModules, dioDevices, devicesByPowerSource);
        
        // Generate VRM Port Usage Report
        generateVRMReport(devicesByPowerSource);

        // Generate Power Branch Report (buck converters and other intermediate converters)
        generatePowerBranchReport(contract);

        // Print power source assignments
        printPowerSourceReport(devicesByPowerSource);
    }
    
    private static void generatePDHReport(Contract2026 contract, SwerveInstance[] swerveModules, List<DeviceInfo> dioDevices, Map<PowerSource, List<String>> devicesByPowerSource) {
        // Track motor and additional connections separately for accurate conflict detection.
        // Multiple non-motor devices on the same PDH port is allowed (e.g., two buck converters).
        Map<PDHPort, List<String>> motorPDHUsage = new TreeMap<>();
        Map<PDHPort, List<String>> additionalPDHUsage = new TreeMap<>();

        // Build PDH usage from all motors collected by autoDiscoverDevices and swerve handling
        for (PDHPort port : PDHPort.values()) {
            try {
                PowerSource ps = PowerSource.valueOf(port.name());
                List<String> devices = devicesByPowerSource.get(ps);
                if (devices != null && !devices.isEmpty()) {
                    motorPDHUsage.computeIfAbsent(port, k -> new ArrayList<>()).addAll(devices);
                }
            } catch (IllegalArgumentException e) {
                // No matching PowerSource for this PDHPort, skip
            }
        }

        // Collect additional PDH connections (VRMs, buck converters, etc.)
        // Multiple non-motor devices per port are allowed and will NOT be flagged as conflicts
        Map<PDHPort, List<String>> additionalConnections = contract.getAdditionalPDHConnections();
        for (Map.Entry<PDHPort, List<String>> entry : additionalConnections.entrySet()) {
            additionalPDHUsage.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).addAll(entry.getValue());
        }

        // Detect conflicts:
        // - Multiple motors on same port: always a conflict
        // - Motor + non-motor device on same port: conflict
        // - Multiple non-motor devices on same port: allowed
        List<String> conflicts = new ArrayList<>();
        for (Map.Entry<PDHPort, List<String>> entry : motorPDHUsage.entrySet()) {
            PDHPort port = entry.getKey();
            List<String> motors = entry.getValue();
            if (motors.size() > 1) {
                conflicts.add(String.format("  *** CONFLICT *** %s assigned to multiple MOTORS: %s",
                        port, String.join(", ", motors)));
            }
            if (additionalPDHUsage.containsKey(port)) {
                List<String> others = additionalPDHUsage.get(port);
                conflicts.add(String.format("  *** CONFLICT *** %s shared between motor(s) and non-motor device(s): motors=[%s], other=[%s]",
                        port, String.join(", ", motors), String.join(", ", others)));
            }
        }

        // Print PDH usage in port order
        System.out.println("-".repeat(80));
        System.out.println("PDH Port Assignments");
        System.out.println("-".repeat(80));

        int usedPortCount = 0;
        for (PDHPort port : PDHPort.values()) {
            List<String> motors = motorPDHUsage.getOrDefault(port, Collections.emptyList());
            List<String> additionals = additionalPDHUsage.getOrDefault(port, Collections.emptyList());
            List<String> all = new ArrayList<>(motors);
            all.addAll(additionals);

            boolean isConflict = motors.size() > 1 || (!motors.isEmpty() && !additionals.isEmpty());

            if (all.isEmpty()) {
                System.out.printf("  %s: (unused)\n", port);
            } else if (isConflict) {
                System.out.printf("  %s: *** CONFLICT *** %s\n", port, String.join(", ", all));
                usedPortCount++;
            } else {
                System.out.printf("  %s: %s\n", port, String.join(", ", all));
                usedPortCount++;
            }
        }

        System.out.println();
        System.out.println("=".repeat(80));
        System.out.println("Total PDH ports used: " + usedPortCount + " / " + PDHPort.values().length);
        System.out.println("=".repeat(80));

        // Print conflict summary if any
        if (!conflicts.isEmpty()) {
            System.out.println();
            System.out.println("!".repeat(80));
            System.out.println("PDH CONFLICTS DETECTED - Fix these before deploying to the robot!");
            System.out.println("!".repeat(80));
            for (String conflict : conflicts) {
                System.out.println(conflict);
            }
            System.out.println("!".repeat(80));
        }

        System.out.println();
    }
    
    /**
     * Automatically discovers devices in the contract using reflection.
     * Scans all public methods with no parameters that return device types.
     */
    private static void autoDiscoverDevices(
            Contract2026 contract,
            Map<CANBusId, List<CANDevice>> devicesByBus,
            List<DeviceInfo> dioDevices,
            List<DeviceInfo> pwmDevices,
            Map<PowerSource, List<String>> devicesByPowerSource) {
        
        Method[] methods = contract.getClass().getMethods();
        
        for (Method method : methods) {
            // Skip methods with parameters (swerve drives are handled separately)
            if (method.getParameterCount() > 0) {
                continue;
            }
            
            try {
                Class<?> returnType = method.getReturnType();
                
                // Check if method returns CANMotorControllerInfo
                if (returnType.equals(CANMotorControllerInfo.class)) {
                    CANMotorControllerInfo motor = (CANMotorControllerInfo) method.invoke(contract);
                    if (motor != null) {
                        CANDevice device = new CANDevice(
                            motor.name(),
                            motor.busId(),
                            motor.deviceId(),
                            motor.type().toString(),
                            motor.pdhPort(),
                            null
                        );
                        devicesByBus.computeIfAbsent(motor.busId(), k -> new ArrayList<>()).add(device);
                        
                        // Track power source (motors don't have powerFrom, use PDH port if available)
                        if (motor.pdhPort() != null) {
                            PowerSource powerSource = PowerSource.valueOf(motor.pdhPort().name());
                            devicesByPowerSource.computeIfAbsent(powerSource, k -> new ArrayList<>()).add(motor.name());
                        }
                    }
                }
                // Check if method returns DeviceInfo
                else if (returnType.equals(DeviceInfo.class)) {
                    DeviceInfo device = (DeviceInfo) method.invoke(contract);
                    if (device != null) {
                        // Track power source
                        if (device.powerFrom != null) {
                            devicesByPowerSource.computeIfAbsent(device.powerFrom, k -> new ArrayList<>()).add(device.name);
                        }
                        
                        // Categorize device based on method name and properties
                        String methodName = method.getName().toLowerCase();
                        
                        // PWM devices - identified by method name patterns (servo, pwm in name)
                        if (methodName.contains("servo") || methodName.contains("pwm")) {
                            pwmDevices.add(device);
                        }
                        // CAN devices - have CAN bus ID (encoders are CAN devices, check before DIO)
                        else if (device.canBusId != null && device.channel > 0) {
                            CANDevice canDevice = new CANDevice(
                                device.name,
                                device.canBusId,
                                device.channel,
                                "Sensor",
                                null,
                                device.powerFrom
                            );
                            devicesByBus.computeIfAbsent(device.canBusId, k -> new ArrayList<>()).add(canDevice);
                        }
                        // DIO devices - powered from RIO and no CAN bus ID
                        else if (device.powerFrom != null && device.powerFrom.equals(PowerSource.RIO)) {
                            dioDevices.add(device);
                        }
                    }
                }
                // Check if method returns IMUInfo
                else if (returnType.equals(IMUInfo.class)) {
                    IMUInfo imu = (IMUInfo) method.invoke(contract);
                    if (imu != null && imu.canBusId() != null) {
                        CANDevice device = new CANDevice(
                            imu.name(),
                            imu.canBusId(),
                            imu.deviceId(),
                            "IMU",
                            null,
                            imu.powerFrom()
                        );
                        devicesByBus.computeIfAbsent(imu.canBusId(), k -> new ArrayList<>()).add(device);
                        
                        // Track power source
                        if (imu.powerFrom() != null) {
                            devicesByPowerSource.computeIfAbsent(imu.powerFrom(), k -> new ArrayList<>()).add(imu.name());
                        }
                    }
                }
                
            } catch (Exception e) {
                // Skip methods that fail (e.g., isReady() checks, abstract methods, etc.)
                // This is expected and normal
            }
        }
    }
    
    /**
     * Generates VRM port usage report.
     */
    private static void generateVRMReport(Map<PowerSource, List<String>> devicesByPowerSource) {
        // Define all VRM ports in order
        PowerSource[] vrm1Ports = {
            PowerSource.VRM1_12V_2A, PowerSource.VRM1_12V_2B,
            PowerSource.VRM1_12V_500MA, PowerSource.VRM1_12V_500MB,
            PowerSource.VRM1_5V_2A, PowerSource.VRM1_5V_2B,
            PowerSource.VRM1_5V_500MA, PowerSource.VRM1_5V_500MB
        };

        PowerSource[] vrm2Ports = {
            PowerSource.VRM2_12V_2A, PowerSource.VRM2_12V_2B,
            PowerSource.VRM2_12V_500MA, PowerSource.VRM2_12V_500MB,
            PowerSource.VRM2_5V_2A, PowerSource.VRM2_5V_2B,
            PowerSource.VRM2_5V_500MA, PowerSource.VRM2_5V_500MB
        };

        // Check if VRM1 has any connections
        int vrm1Count = 0;
        for (PowerSource port : vrm1Ports) {
            if (devicesByPowerSource.containsKey(port)) {
                vrm1Count += devicesByPowerSource.get(port).size();
            }
        }

        // Print VRM1 if it has connections
        if (vrm1Count > 0) {
            List<String> vrm1Conflicts = new ArrayList<>();
            System.out.println("-".repeat(80));
            System.out.println("VRM1 Port Assignments");
            System.out.println("-".repeat(80));

            for (PowerSource port : vrm1Ports) {
                List<String> devices = devicesByPowerSource.get(port);
                if (devices == null || devices.isEmpty()) {
                    System.out.printf("  %s: (unused)\n", port);
                } else if (devices.size() == 1) {
                    System.out.printf("  %s: %s\n", port, devices.get(0));
                } else {
                    System.out.printf("  %s: *** CONFLICT *** %s\n", port, String.join(", ", devices));
                    vrm1Conflicts.add(String.format("  *** CONFLICT *** %s has multiple devices: %s",
                            port, String.join(", ", devices)));
                }
            }

            System.out.println();
            System.out.println("=".repeat(80));
            System.out.println("Total VRM1 ports used: " + vrm1Count);
            System.out.println("=".repeat(80));

            if (!vrm1Conflicts.isEmpty()) {
                System.out.println();
                System.out.println("!".repeat(80));
                System.out.println("VRM1 CONFLICTS DETECTED - Each VRM output port supports only one connection!");
                System.out.println("!".repeat(80));
                for (String conflict : vrm1Conflicts) {
                    System.out.println(conflict);
                }
                System.out.println("!".repeat(80));
            }

            System.out.println();
        }

        // Check if VRM2 has any connections
        int vrm2Count = 0;
        for (PowerSource port : vrm2Ports) {
            if (devicesByPowerSource.containsKey(port)) {
                vrm2Count += devicesByPowerSource.get(port).size();
            }
        }

        // Print VRM2 if it has connections
        if (vrm2Count > 0) {
            List<String> vrm2Conflicts = new ArrayList<>();
            System.out.println("-".repeat(80));
            System.out.println("VRM2 Port Assignments");
            System.out.println("-".repeat(80));

            for (PowerSource port : vrm2Ports) {
                List<String> devices = devicesByPowerSource.get(port);
                if (devices == null || devices.isEmpty()) {
                    System.out.printf("  %s: (unused)\n", port);
                } else if (devices.size() == 1) {
                    System.out.printf("  %s: %s\n", port, devices.get(0));
                } else {
                    System.out.printf("  %s: *** CONFLICT *** %s\n", port, String.join(", ", devices));
                    vrm2Conflicts.add(String.format("  *** CONFLICT *** %s has multiple devices: %s",
                            port, String.join(", ", devices)));
                }
            }

            System.out.println();
            System.out.println("=".repeat(80));
            System.out.println("Total VRM2 ports used: " + vrm2Count);
            System.out.println("=".repeat(80));

            if (!vrm2Conflicts.isEmpty()) {
                System.out.println();
                System.out.println("!".repeat(80));
                System.out.println("VRM2 CONFLICTS DETECTED - Each VRM output port supports only one connection!");
                System.out.println("!".repeat(80));
                for (String conflict : vrm2Conflicts) {
                    System.out.println(conflict);
                }
                System.out.println("!".repeat(80));
            }

            System.out.println();
        }
    }
    
    /**
     * Generates power branch report for intermediate converters (buck converters, VRMs, etc.)
     * Shows the chain: PDH port -> converter -> downstream devices.
     */
    private static void generatePowerBranchReport(Contract2026 contract) {
        Map<String, List<String>> branches = contract.getAdditionalPowerBranches();
        if (branches.isEmpty()) {
            return;
        }

        // Build reverse map: branch/converter name -> PDH port(s) that supply it
        Map<String, List<String>> branchToPDH = new HashMap<>();
        Map<PDHPort, List<String>> pdhConnections = contract.getAdditionalPDHConnections();
        for (Map.Entry<PDHPort, List<String>> entry : pdhConnections.entrySet()) {
            for (String device : entry.getValue()) {
                branchToPDH.computeIfAbsent(device, k -> new ArrayList<>()).add(entry.getKey().toString());
            }
        }

        System.out.println("-".repeat(80));
        System.out.println("Power Branch Assignments (Intermediate Converters)");
        System.out.println("-".repeat(80));

        List<String> sortedBranches = new ArrayList<>(branches.keySet());
        Collections.sort(sortedBranches);

        for (String branchName : sortedBranches) {
            List<String> pdhPorts = branchToPDH.getOrDefault(branchName, new ArrayList<>());
            String pdhInfo = pdhPorts.isEmpty() ? "unknown PDH port" : String.join(", ", pdhPorts);
            for (String device : branches.get(branchName)) {
                System.out.printf("  %s (%s) -> %s\n", branchName, pdhInfo, device);
            }
        }

        System.out.println();
        System.out.println("=".repeat(80));
        System.out.println("Total power branches: " + branches.size());
        System.out.println("=".repeat(80));
        System.out.println();
    }

    /**
     * Prints power source report showing which devices are powered by each source.
     */
    private static void printPowerSourceReport(Map<PowerSource, List<String>> devicesByPowerSource) {
        System.out.println("-".repeat(80));
        System.out.println("Additional Power Source Assignments");
        System.out.println("-".repeat(80));
        
        // Sort power sources for consistent output
        List<PowerSource> sortedSources = new ArrayList<>(devicesByPowerSource.keySet());
        sortedSources.sort(Comparator.comparing(PowerSource::toString));
        
        int count = 0;
        for (PowerSource source : sortedSources) {
            // Skip MOTOR, PDH*, RIO, VRM*, and NONE power sources
            String sourceName = source.toString();
            if (sourceName.equals("MOTOR") || sourceName.equals("RIO") || sourceName.equals("NONE")
                || sourceName.startsWith("PDH") || sourceName.startsWith("VRM")) {
                continue;
            }
            
            List<String> devices = devicesByPowerSource.get(source);
            devices.sort(String::compareTo);
            String deviceList = String.join(", ", devices);
            System.out.println("  " + source + " -> " + deviceList);
            count++;
        }
        
        System.out.println();
        System.out.println("=".repeat(80));
        System.out.println("Total additional power sources used: " + count);
        System.out.println("=".repeat(80));
    }
}
