package competition;

import org.junit.Test;

public class RobotInitTest extends BaseCompetitionTest {
    @Test
    public void testDefaultSystem() {
        getInjectorComponent().subsystemDefaultCommandMap();
        getInjectorComponent().operatorCommandMap();
        throw new RuntimeException("This test just checks that the robot can initialize without throwing exceptions");
    }
}