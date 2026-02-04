package competition.electrical_contract;

import javax.inject.Inject;

public class UnitTestCompetitionContract extends Contract2026 {

    @Inject
    public UnitTestCompetitionContract() {}

    @Override
    public boolean isLeftShooterReady() {
        return true;
    }

    @Override
    public boolean isMiddleShooterReady() {
        return true;
    }

    @Override
    public boolean isRightShooterReady() {
        return true;
    }

    @Override
    public boolean isFuelIntakeMotorReady() {
        return true;
    }

    @Override
    public boolean isIntakeDeployReady() {
        return true;
    }
}
