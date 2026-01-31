package competition.electrical_contract;

import javax.inject.Inject;

public class UnitTestCompetitionContract extends Contract2026 {

    @Inject
    public UnitTestCompetitionContract() {}

    @Override
    public boolean isShooterReady() {
        return true;
    }

    @Override
    public boolean isIntakeDeployReady() {
        return true;
    }
}
