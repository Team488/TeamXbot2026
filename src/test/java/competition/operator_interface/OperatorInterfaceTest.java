package competition.operator_interface;

import static org.junit.Assert.assertSame;

import org.junit.Test;

import competition.BaseCompetitionTest;
import edu.wpi.first.wpilibj.MockXboxControllerAdapter;

public class OperatorInterfaceTest extends BaseCompetitionTest {
    @Test
    public void testDriverGamepadOverridesDemoGamepad() {
        OperatorInterface oi = getInjectorComponent().operatorInterface();
        var driver = (MockXboxControllerAdapter) oi.driverGamepad;

        assertSame(oi.demoGamepad, oi.getActiveDriveGamepad());

        driver.setLeftStick(0, 0.5);
        assertSame(oi.driverGamepad, oi.getActiveDriveGamepad());

        driver.setLeftStick(0, 0);
        driver.setRightTrigger(0.5);
        assertSame(oi.driverGamepad, oi.getActiveDriveGamepad());
    }
}
