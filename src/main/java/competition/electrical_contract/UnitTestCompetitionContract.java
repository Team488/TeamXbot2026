package competition.electrical_contract;

import javax.inject.Inject;

public class UnitTestCompetitionContract extends CompetitionContract {

    @Inject
    public UnitTestCompetitionContract() {}

    @Override
    public boolean isLeftShooterReady() {
        return true;
    }

    public boolean isMiddleShooterReady() {
        return true;
    }

    public boolean isRightShooterReady() {
        return true;
    }
}
