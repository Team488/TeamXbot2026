package competition;

import competition.injection.components.CompetitionTestComponent;
import competition.injection.components.DaggerCompetitionTestComponent;
import xbot.common.injection.BaseWPITest;

public class BaseCompetitionTest extends BaseWPITest{
    @Override
    protected CompetitionTestComponent createDaggerComponent() {
        return DaggerCompetitionTestComponent.create();
    }

    @Override
    protected CompetitionTestComponent getInjectorComponent() {
        return (CompetitionTestComponent)super.getInjectorComponent();
    }
}
