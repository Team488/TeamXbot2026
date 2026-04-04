package competition.auto_programs;

import xbot.common.properties.PropertyFactory;
import xbot.common.subsystems.autonomous.AutonomousCommandSelector;

import javax.inject.Inject;

public class CollectAndShootTwiceCommand extends BaseAutonomousSequentialCommandGroup {

    @Inject
    public CollectAndShootTwiceCommand(
            AutonomousCommandSelector autoSelector,
            AutoCommandFactory auto,
            PropertyFactory pf) {
        super(autoSelector);

        pf.setPrefix(this.getName());
        var firstShotTimeout = pf.createPersistentProperty("First shot timeout seconds", 3.0);
        var secondShotTimeout = pf.createPersistentProperty("Second shot timeout seconds", 10.0);

        addCommands(
            auto.extendIntake(),
            auto.collectFromNeutralZone()
                .alongWith(auto.statusMessage("Driving to neutral zone and back")),
            auto.driveToAllianceAndShoot(firstShotTimeout::get)
                .alongWith(auto.statusMessage("Driving to alliance and shoot")),
            auto.stopShooter(),
            auto.collectFromNeutralZone()
                .alongWith(auto.statusMessage("Driving to neutral zone and back")),
            auto.driveToAllianceAndShoot(secondShotTimeout::get)
                .alongWith(auto.statusMessage("Driving to alliance and shoot"))
        );
    }
}
