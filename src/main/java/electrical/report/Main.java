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
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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
        Integer busPosition; // null if not listed in getCanBusConnectionOrder()
        
        CANDevice(String name, CANBusId busId, int deviceId, String type, PDHPort pdhPort, PowerSource powerSource) {
            this(name, busId, deviceId, type, pdhPort, powerSource, null);
        }
        
        CANDevice(String name, CANBusId busId, int deviceId, String type, PDHPort pdhPort, PowerSource powerSource, Integer busPosition) {
            this.name = name;
            this.busId = busId;
            this.deviceId = deviceId;
            this.type = type;
            this.pdhPort = pdhPort;
            this.powerSource = powerSource;
            this.busPosition = busPosition;
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
    
    public static void main(String[] args) throws Exception {
        // Determine output file: default is electrical-report.txt, override with -f <filename>
        String outputFile = "electrical-report.txt";
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals("-f")) {
                outputFile = args[i + 1];
                break;
            }
        }
        PrintStream fileOut = new PrintStream(new FileOutputStream(new File(outputFile)));
        System.setOut(fileOut);
        System.err.println("Writing electrical report to: " + outputFile);

        System.out.println("=".repeat(80));
        System.out.println("Electrical Contract Report");
        System.out.println("=".repeat(80));
        System.out.println();
        
        var contract = createContract();
        System.out.println("Source: " + contract.getClass().getName());

        // Find and print the source file timestamp
        String sourceRelPath = "src/main/java/"
                + contract.getClass().getName().replace('.', '/') + ".java";
        Path sourcePath = Paths.get(sourceRelPath);
        if (Files.exists(sourcePath)) {
            BasicFileAttributes attrs = Files.readAttributes(sourcePath, BasicFileAttributes.class);
            String timestamp = attrs.lastModifiedTime()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
            System.out.println("Source last modified: " + timestamp);
        }
        System.out.println();
        
        Map<CANBusId, List<CANDevice>> devicesByBus = new HashMap<>();
        devicesByBus.put(CANBusId.RIO, new ArrayList<>());
        devicesByBus.put(CANBusId.Canivore, new ArrayList<>());
        
        List<DeviceInfo> dioDevices = new ArrayList<>();
        List<DeviceInfo> pwmDevices = new ArrayList<>();
        Map<PowerSource, List<String>> devicesByPowerSource = new HashMap<>();
        
        // Auto-discover all devices using reflection
        List<String> positionWarnings = new ArrayList<>();
        autoDiscoverDevices(contract, devicesByBus, dioDevices, pwmDevices, devicesByPowerSource, positionWarnings);
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
        
        // Generate CAN Bus Connection Order report
        generateConnectionOrderReport(devicesByBus, positionWarnings);
        
        // Print DIO devices
        System.out.println("-".repeat(80));
        System.out.println("RIO Digital I/O Devices (" + dioDevices.size() + " devices)");
        System.out.println("-".repeat(80));
        dioDevices.sort(Comparator.comparingInt(d -> d.channel));
        for (DeviceInfo device : dioDevices) {
            System.out.printf("  DIO %2d: %s\n", device.channel, device.name);
        }
        System.out.println();
        
        // Print PWM devices
        System.out.println("-".repeat(80));
        System.out.println("PWM Devices (" + pwmDevices.size() + " devices)");
        System.out.println("-".repeat(80));
        pwmDevices.sort(Comparator.comparingInt(d -> d.channel));
        for (DeviceInfo device : pwmDevices) {
            System.out.printf("  PWM %2d: %s\n", device.channel, device.name);
        }
        System.out.println();
        
        System.out.println("=".repeat(80));
        System.out.println("Total devices: CAN=" 
            + (devicesByBus.get(CANBusId.RIO).size() + devicesByBus.get(CANBusId.Canivore).size())
            + ", DIO=" + dioDevices.size() + ", PWM=" + pwmDevices.size());
        System.out.println("=".repeat(80));
        System.out.println();
        
        // Generate PDH Port Usage Report
        generatePDHReport(contract, dioDevices, devicesByPowerSource);
        
        // Generate VRM Port Usage Report
        generateVRMReport(devicesByPowerSource);

        // Generate Power Branch Report (buck converters and other intermediate converters)
        generatePowerBranchReport(contract, devicesByPowerSource);

        // Print power source assignments
        printPowerSourceReport(devicesByPowerSource);
    }
    
    private static void generatePDHReport(
            Contract2026 contract,
            List<DeviceInfo> dioDevices, Map<PowerSource, List<String>> devicesByPowerSource) {
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
            boolean isNoConnect = !all.isEmpty() && all.stream().allMatch(s -> s.equals("No_Connect"));

            if (all.isEmpty() || isNoConnect) {
                System.out.printf("  %s: (No Connect)\n", port);
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
     * Lists all CAN devices sorted by physical bus connection order as documented
     * via getCanBusConnectionOrder(). Devices not listed appear last with "Pos ?".
     */
    private static void generateConnectionOrderReport(
            Map<CANBusId, List<CANDevice>> devicesByBus,
            List<String> positionWarnings) {
        System.out.println("-".repeat(80));
        System.out.println("CAN Bus Connection Order");
        System.out.println("-".repeat(80));

        for (CANBusId busId : Arrays.asList(CANBusId.RIO, CANBusId.Canivore)) {
            List<CANDevice> devices = new ArrayList<>(devicesByBus.get(busId));
            if (devices.isEmpty()) {
                continue;
            }

            String busName = busId.equals(CANBusId.Canivore) ? "CANBusId[id=Canivore]" : "CANBusId[id=rio]";
            System.out.println("  " + busName + ":");

            // Sort: annotated devices first (ascending by position), then unspecified (ascending by CAN ID)
            devices.sort((a, b) -> {
                if (a.busPosition != null && b.busPosition != null) {
                    return Integer.compare(a.busPosition, b.busPosition);
                } else if (a.busPosition != null) {
                    return -1;
                } else if (b.busPosition != null) {
                    return 1;
                } else {
                    return Integer.compare(a.deviceId, b.deviceId);
                }
            });

            for (CANDevice device : devices) {
                if (device.busPosition != null) {
                    System.out.printf("    Pos %2d: ID %2d  %-40s [%s]%n",
                            device.busPosition, device.deviceId, device.name, device.type);
                } else {
                    System.out.printf("    Pos  ?: ID %2d  %-40s [%s]%n",
                            device.deviceId, device.name, device.type);
                }
            }
            System.out.println();
        }

        if (!positionWarnings.isEmpty()) {
            System.out.println("WARNING: The following CAN devices are missing from getCanBusConnectionOrder():");
            for (String warning : positionWarnings) {
                System.out.println("  - " + warning);
            }
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
            Map<PowerSource, List<String>> devicesByPowerSource,
            List<String> positionWarnings) {

        // Build (busId, canId) -> busPosition lookup from contract's connection order list
        Map<CANBusId, Map<Integer, Integer>> positionLookup = new HashMap<>();
        for (Contract2026.CanBusOrderEntry entry : contract.getCanBusConnectionOrder()) {
            positionLookup
                    .computeIfAbsent(entry.busId(), k -> new HashMap<>())
                    .put(entry.canId(), entry.busPosition());
        }

        Method[] methods = contract.getClass().getMethods();
        
        for (Method method : methods) {
            // Skip methods with parameters
            if (method.getParameterCount() > 0) {
                continue;
            }
            
            try {
                Class<?> returnType = method.getReturnType();
                
                // Check if method returns CANMotorControllerInfo
                if (returnType.equals(CANMotorControllerInfo.class)) {
                    CANMotorControllerInfo motor = (CANMotorControllerInfo) method.invoke(contract);
                    if (motor != null) {
                        Integer busPosition = positionLookup
                                .getOrDefault(motor.busId(), Map.of())
                                .get(motor.deviceId());
                        if (busPosition == null) {
                            positionWarnings.add(motor.name() + " (ID " + motor.deviceId() + " on " + motor.busId() + ")");
                        }
                        CANDevice device = new CANDevice(
                            motor.name(),
                            motor.busId(),
                            motor.deviceId(),
                            motor.type().toString(),
                            motor.pdhPort(),
                            null,
                            busPosition
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
                        // DIO devices - identified by the "DIO" suffix on the device name (e.g. "ClimbHomeDIO").
                        // This is more reliable than checking PowerSource.RIO because canBusId defaults to
                        // CANBusId.RIO for all DeviceInfo, making PowerSource.RIO alone ambiguous.
                        else if (device.name != null && device.name.endsWith("DIO")) {
                            dioDevices.add(device);
                        }
                        // CAN devices - have CAN bus ID and no RIO power source
                        else if (device.canBusId != null && device.channel > 0) {
                            Integer busPosition = positionLookup
                                    .getOrDefault(device.canBusId, Map.of())
                                    .get(device.channel);
                            if (busPosition == null) {
                                positionWarnings.add(device.name + " (ID " + device.channel + " on " + device.canBusId + ")");
                            }
                            String canDeviceType = methodName.contains("encoder") ? "CANCoder" : "Sensor";
                            CANDevice canDevice = new CANDevice(
                                device.name,
                                device.canBusId,
                                device.channel,
                                canDeviceType,
                                null,
                                device.powerFrom,
                                busPosition
                            );
                            devicesByBus.computeIfAbsent(device.canBusId, k -> new ArrayList<>()).add(canDevice);
                        }
                    }
                }
                // Check if method returns IMUInfo
                else if (returnType.equals(IMUInfo.class)) {
                    IMUInfo imu = (IMUInfo) method.invoke(contract);
                    if (imu != null && imu.canBusId() != null) {
                        Integer busPosition = positionLookup
                                .getOrDefault(imu.canBusId(), Map.of())
                                .get(imu.deviceId());
                        if (busPosition == null) {
                            positionWarnings.add(imu.name() + " (ID " + imu.deviceId() + " on " + imu.canBusId() + ")");
                        }
                        CANDevice device = new CANDevice(
                            imu.name(),
                            imu.canBusId(),
                            imu.deviceId(),
                            "IMU",
                            null,
                            imu.powerFrom(),
                            busPosition
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

        // Swerve devices use parameterized getters — call them explicitly for each corner.
        String[] swerveLabels = {"FrontLeftDrive", "FrontRightDrive", "RearLeftDrive", "RearRightDrive"};
        for (String label : swerveLabels) {
            SwerveInstance si = new SwerveInstance(label);

            CANMotorControllerInfo drive = contract.getDriveMotor(si);
            if (drive != null) {
                Integer busPosition = positionLookup.getOrDefault(drive.busId(), Map.of()).get(drive.deviceId());
                if (busPosition == null) {
                    positionWarnings.add(drive.name() + " (ID " + drive.deviceId() + " on " + drive.busId() + ")");
                }
                devicesByBus.computeIfAbsent(drive.busId(), k -> new ArrayList<>()).add(
                    new CANDevice(drive.name(), drive.busId(), drive.deviceId(), drive.type().toString(), drive.pdhPort(), null, busPosition));
                if (drive.pdhPort() != null) {
                    devicesByPowerSource.computeIfAbsent(PowerSource.valueOf(drive.pdhPort().name()), k -> new ArrayList<>()).add(drive.name());
                }
            }

            CANMotorControllerInfo steering = contract.getSteeringMotor(si);
            if (steering != null) {
                Integer busPosition = positionLookup.getOrDefault(steering.busId(), Map.of()).get(steering.deviceId());
                if (busPosition == null) {
                    positionWarnings.add(steering.name() + " (ID " + steering.deviceId() + " on " + steering.busId() + ")");
                }
                devicesByBus.computeIfAbsent(steering.busId(), k -> new ArrayList<>()).add(
                    new CANDevice(steering.name(), steering.busId(), steering.deviceId(), steering.type().toString(), steering.pdhPort(), null, busPosition));
                if (steering.pdhPort() != null) {
                    devicesByPowerSource.computeIfAbsent(PowerSource.valueOf(steering.pdhPort().name()), k -> new ArrayList<>()).add(steering.name());
                }
            }

            DeviceInfo encoder = contract.getSteeringEncoder(si);
            if (encoder != null && encoder.canBusId != null && encoder.channel > 0) {
                Integer busPosition = positionLookup.getOrDefault(encoder.canBusId, Map.of()).get(encoder.channel);
                if (busPosition == null) {
                    positionWarnings.add(encoder.name + " (ID " + encoder.channel + " on " + encoder.canBusId + ")");
                }
                devicesByBus.computeIfAbsent(encoder.canBusId, k -> new ArrayList<>()).add(
                    new CANDevice(encoder.name, encoder.canBusId, encoder.channel, "CANCoder", null, encoder.powerFrom, busPosition));
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
                    System.out.printf("  %s: (No Connect)\n", port);
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
                    System.out.printf("  %s: (No Connect)\n", port);
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
    private static void generatePowerBranchReport(Contract2026 contract, Map<PowerSource, List<String>> devicesByPowerSource) {
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

        // Build reverse map: branch/converter name -> VRM port(s) that supply it
        Map<String, List<String>> branchToVRM = new HashMap<>();
        for (Map.Entry<PowerSource, List<String>> entry : devicesByPowerSource.entrySet()) {
            String sourceName = entry.getKey().toString();
            if (sourceName.startsWith("VRM")) {
                for (String device : entry.getValue()) {
                    if (branches.containsKey(device)) {
                        branchToVRM.computeIfAbsent(device, k -> new ArrayList<>()).add(sourceName);
                    }
                }
            }
        }

        System.out.println("-".repeat(80));
        System.out.println("Power Branch Assignments (Intermediate Converters)");
        System.out.println("-".repeat(80));

        List<String> sortedBranches = new ArrayList<>(branches.keySet());
        Collections.sort(sortedBranches);

        for (String branchName : sortedBranches) {
            List<String> pdhPorts = branchToPDH.getOrDefault(branchName, new ArrayList<>());
            List<String> vrmPorts = branchToVRM.getOrDefault(branchName, new ArrayList<>());
            List<String> allSources = new ArrayList<>();
            allSources.addAll(pdhPorts);
            allSources.addAll(vrmPorts);
            String sourceInfo = allSources.isEmpty() ? "" : " (" + String.join(", ", allSources) + ")";
            for (String device : branches.get(branchName)) {
                System.out.printf("  %s%s -> %s\n", branchName, sourceInfo, device);
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
            boolean isInternalSource = sourceName.equals("MOTOR") || sourceName.equals("RIO") || sourceName.equals("NONE");
            boolean isPdhOrVrm = sourceName.startsWith("PDH") || sourceName.startsWith("VRM");
            if (isInternalSource || isPdhOrVrm) {
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
