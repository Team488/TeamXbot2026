package competition.electrical_contract;

import javax.inject.Inject;

public class UnitTestCompetitionContract extends CompetitionContract {

    @Inject
    public UnitTestCompetitionContract() {}

    @Override
    public boolean isShooterReady() {
        return true;
    }
}
