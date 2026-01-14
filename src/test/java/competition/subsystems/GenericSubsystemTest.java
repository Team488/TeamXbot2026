package competition.subsystems;

import competition.BaseCompetitionTest;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANMotorController;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

@FixMethodOrder(MethodSorters.NAME_ASCENDING) // Prevent parallel running of tests
public class GenericSubsystemTest extends BaseCompetitionTest {

    @Test
    public void testAllSubsystemPeriodic() throws Exception {
        // Find all subsystem implementations via reflection
        List<Object> subsystems = new ArrayList<>();
        for (var injectorMethod : getInjectorComponent().getClass().getMethods()) {
            if (injectorMethod.getParameterCount() != 0) {
                continue;
            }
            if (BaseSubsystem.class.isAssignableFrom(injectorMethod.getReturnType())) {
                injectorMethod.setAccessible(true);
                subsystems.add(injectorMethod.invoke(getInjectorComponent()));
            }
        }
        assertNotEquals(0, subsystems.size());

        for (Object subsystem : subsystems) {
            // Run each subsystem refreshDataFrame method
            Method refreshDataFrameMethod = subsystem.getClass().getMethod("refreshDataFrame");
            refreshDataFrameMethod.setAccessible(true);
            try {
                refreshDataFrameMethod.invoke(subsystem);
            } catch (Exception e) {
                fail("Subsystem " + subsystem.getClass().getName() + " failed to call refreshDataFrame:\n" + e);
            }
        }

        for (Object subsystem : subsystems) {
            // Run each subsystem periodic method
            Method periodicMethod = subsystem.getClass().getMethod("periodic");
            periodicMethod.setAccessible(true);
            try {
                periodicMethod.invoke(subsystem);
            } catch (Exception e) {
                fail("Subsystem " + subsystem.getClass().getName() + " failed to call periodic:\n" + e);
            }
        }
    }

    @Test
    public void testAllSubsystemsCallPeriodicOnMotorControllers() throws Exception {
        // Find all subsystem implementations via reflection
        List<Object> subsystems = new ArrayList<>();
        for (var injectorMethod : getInjectorComponent().getClass().getMethods()) {
            if (injectorMethod.getParameterCount() != 0) {
                continue;
            }
            if (BaseSubsystem.class.isAssignableFrom(injectorMethod.getReturnType())) {
                injectorMethod.setAccessible(true);
                subsystems.add(injectorMethod.invoke(getInjectorComponent()));
            }
        }
        assertNotEquals(0, subsystems.size());

        Map<Object, List<Object>> subsystemMotorControllerMap = new HashMap<>();

        // For each subsystem, find all the motor controllers
        for (Object subsystem : subsystems) {
            List<Object> motorControllers = new ArrayList<>();
            for (Field field : subsystem.getClass().getFields()) {
                if (XCANMotorController.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    // This isn't safe for parallel test execution, so we need to force
                    // tests in this class to run sequentially
                    field.set(subsystem, Mockito.spy(field.get(subsystem)));
                    // Replace the original motor controllers with the mocked ones
                    motorControllers.add(field.get(subsystem));
                }
            }
            subsystemMotorControllerMap.put(subsystem, motorControllers);
        }

        // Run each subsystem refreshDataFrame method
        for (Object subsystem : subsystems) {
            Method refreshDataFrameMethod = subsystem.getClass().getMethod("refreshDataFrame");
            refreshDataFrameMethod.setAccessible(true);
            try {
                refreshDataFrameMethod.invoke(subsystem);
            } catch (Exception e) {
                fail("Subsystem " + subsystem.getClass().getName() + " failed to call refreshDataFrame:\n" + e);
            }
        }

        // Run each subsystem periodic method
        for (Object subsystem : subsystems) {
            Method periodicMethod = subsystem.getClass().getMethod("periodic");
            periodicMethod.setAccessible(true);
            try {
                periodicMethod.invoke(subsystem);
            } catch (Exception e) {
                fail("Subsystem " + subsystem.getClass().getName() + " failed to call periodic:\n" + e);
            }
        }

        // Check that each motor controller periodic method was called
        for (Object subsystem : subsystems) {
            for (Object motorController : subsystemMotorControllerMap.get(subsystem)) {
                Mockito.verify((XCANMotorController)motorController, Mockito.atLeastOnce()
                        .description("Subsystem " + subsystem.getClass().getName() + " did not call periodic on a motor controller.")).periodic();
            }
        }
    }
}