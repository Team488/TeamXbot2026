package competition.subsystems;

import competition.BaseCompetitionTest;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import xbot.common.command.BaseSubsystem;
import xbot.common.controls.actuators.XCANMotorController;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
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
                failOnMethodException(subsystem, periodicMethod, e);
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
                    var originalMotorController = field.get(subsystem);
                    if (originalMotorController == null) {
                        fail("Subsystem " + subsystem.getClass().getName() + " has a null motor controller "
                                + "field: " + field.getName() + ". Did you forget to mark the subsystem as ready "
                                + "in UnitTestCompetitionContract?");
                    }

                    var mockedMotorController = Mockito.spy((XCANMotorController)originalMotorController);
                    field.set(subsystem, mockedMotorController);
                    motorControllers.add(mockedMotorController);
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
                failOnMethodException(subsystem, refreshDataFrameMethod, e);
            }
        }

        // Run each subsystem periodic method
        for (Object subsystem : subsystems) {
            Method periodicMethod = subsystem.getClass().getMethod("periodic");
            periodicMethod.setAccessible(true);
            try {
                periodicMethod.invoke(subsystem);
            } catch (Exception e) {
                failOnMethodException(subsystem, periodicMethod, e);
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

    /**
     * Helper method to fail a test with a detailed message when a subsystem method throws an exception.
     */
    private void failOnMethodException(Object subsystem, Method method, Throwable e) {
        // Unwrap InvocationTargetException if present to get the root cause
        if (e.getClass() == InvocationTargetException.class) {
            e = ((InvocationTargetException) e).getTargetException();
        }

        // Build a detailed error message with the exception and the first 10 stack trace elements
        var stringBuilder = new StringBuilder();
        stringBuilder.append("An exception was thrown when calling ");
        stringBuilder.append(method.getName());
        stringBuilder.append("() on subsystem ");
        stringBuilder.append(subsystem.getClass().getName());
        stringBuilder.append(":\n");

        stringBuilder.append(e.toString());
        stringBuilder.append("\n");

        Arrays.stream(e.getStackTrace()).limit(10).forEach(element -> {
            stringBuilder.append("\t");
            stringBuilder.append(element.toString());
            stringBuilder.append("\n");
        });

        // Fail the test with the detailed error message
        fail(stringBuilder.toString());
    }
}