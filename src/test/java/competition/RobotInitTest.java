package competition;

import org.junit.Test;

public class RobotInitTest extends BaseCompetitionTest {
    @Test
    public void testDefaultSystem() {
        getInjectorComponent().subsystemDefaultCommandMap();
        getInjectorComponent().operatorCommandMap();
    }
}